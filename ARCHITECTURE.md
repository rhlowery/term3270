# term3270 Architecture

This document provides a high-level overview of the
term3270 terminal emulator architecture, including
system context, containers, and component-level
design.

## 1. System Context Diagram (C4 Level 1)

The System Context diagram shows how the term3270
application interacts with external systems
(Mainframe) and users.

```mermaid
graph LR
    User["Terminal User"] -->|"Keyboard/Mouse"| App["term3270 Emulator"]
    App -->|"TN3270 Protocol"| Host["Mainframe / IBM i"]
    App -->|"TLS/SSL"| Gateway["Secure TN3270 Gateway"]
```

## 2. Container Diagram (C4 Level 2)

The Container diagram illustrates the high-level
components of the application and their
responsibilities.

```mermaid
graph TD
    subgraph "term3270 Application"
        UI["Swing UI / TerminalPanel (FlatLaf Dark)"]
        Logic["Terminal Utilities / Engine"]
        Net["Telnet Client"]
        Config["ConnectionConfig"]
    end
    
    User -->|"Input"| UI
    UI -->|"Events"| Logic
    Logic -->|"Buffer Updates"| UI
    Logic -->|"Data Streams"| Net
    Net -->|"Network I/O"| Host["Mainframe"]
    Config -->|"Geometry, Codepage, Security"| Logic
```

## 3. Core Class Diagram (UML)

The following diagram shows the relationships
between the core classes managing the terminal state
and protocol.

```mermaid
classDiagram
    class ITerminalSession {
        <<interface>>
        +connect(ConnectionConfig config)
        +disconnect()
        +sendAID(AIDKey key)
        +getScreenBuffer() ScreenBuffer
        +getStatus() String
    }
    
    class DefaultTerminalSession {
        -TelnetClient telnet
        -ScreenBuffer buffer
        -IDataStreamParser parser
        +connect(ConnectionConfig config)
    }
    
    class ConnectionConfig {
        <<record>>
        +host : String
        +port : int
        +terminalType : String
        +secure : boolean
        +verifyHostname : boolean
        +codepage : String
        +rows() int
        +cols() int
    }
    
    class IDataStreamParser {
        <<interface>>
        +processByte(byte b)
        +reset()
        +setConverter(EbcdicConverter converter)
    }
    
    class DataStreamParser {
        -ScreenBuffer buffer
        -EbcdicConverter converter
        +processByte(byte b)
        +reset()
    }
    
    class IOrderHandler {
        <<interface>>
        +handle(byte[] data, ScreenBuffer buffer)
    }

    class ScreenBuffer {
        -ScreenCell[] cells
        -int rows
        -int cols
        -EbcdicConverter converter
        +resize(int rows, int cols)
        +writeAtCba(char c)
        +readModified() byte[]
        +readBuffer() byte[]
    }
    
    class EbcdicConverter {
        -Charset charset
        +decode(byte b) char
        +encode(char c) byte
        +toAscii(byte b) char
        +toEbcdic(char c) byte
    }
    
    class ProtocolConstants {
        <<final>>
        +CMD_WRITE : byte
        +ORDER_SBA : byte
        +SF_ID_FILE_TRANSFER : byte
    }
    
    class TelnetClient {
        -IDataStreamParser parser
        +connect(ConnectionConfig config)
        +sendData(byte[] data)
    }
    
    ITerminalSession <|-- DefaultTerminalSession
    IDataStreamParser <|-- DataStreamParser
    DataStreamParser *-- IOrderHandler
    DefaultTerminalSession *-- TelnetClient
    DefaultTerminalSession *-- ScreenBuffer
    DefaultTerminalSession *-- IDataStreamParser
    DefaultTerminalSession ..> ConnectionConfig
    DataStreamParser ..> EbcdicConverter
    DataStreamParser ..> ProtocolConstants
    ScreenBuffer ..> EbcdicConverter
    TelnetClient ..> ConnectionConfig
```

## 4. Data Flow (Activity Diagram)

The following diagram traces the flow of data from
the host to the screen.

```mermaid
stateDiagram-v2
    [*] --> COMMAND
    COMMAND --> WCC: Write/EWA/EW Command
    COMMAND --> READ_PART_ID: Read Partition Command
    COMMAND --> WSF_LEN_1: Write Structured Field Command
    WCC --> DATA: Process WCC bits
    DATA --> SBA_1: ORDER_SBA (0x11)
    DATA --> SF_1: ORDER_SF (0x1D)
    DATA --> RA_1: ORDER_RA (0x3C)
    DATA --> SFE_1: ORDER_SFE (0x29)
    DATA --> SA_1: ORDER_SA (0x28)
    DATA --> DATA: Write Character
    SBA_1 --> SBA_2
    SBA_2 --> DATA: Set CBA
    SF_1 --> DATA: Start Field
    RA_1 --> RA_2
    RA_2 --> RA_CHAR
    RA_CHAR --> DATA: Repeat Character
    READ_PART_ID --> READ_PART_MODE
    READ_PART_MODE --> COMMAND: Handle Query
    WSF_LEN_1 --> WSF_LEN_2
    WSF_LEN_2 --> WSF_ID
    WSF_ID --> WSF_DATA
    WSF_DATA --> COMMAND: Process Field
```

> [!NOTE]
> The activity diagram above uses a simplified
> representation. Actual 3270 parsing involves state
> machines for handling multi-byte orders (SBA, SF,
> SFE, SA, RA, EUA).

## 5. Module Structure

```
term3270/
├── term3270-utilities/  # Core protocol engine
│   └── src/main/java/
│       └── com.rhlowery.term3270/
│           ├── ITerminalSession.java
│           ├── DefaultTerminalSession.java
│           ├── IDataStreamParser.java
│           ├── DataStreamParser.java
│           ├── ScreenBuffer.java
│           ├── ScreenCell.java
│           ├── FieldAttribute.java
│           ├── EbcdicConverter.java
│           ├── AddressConverter.java
│           ├── TelnetClient.java
│           ├── ConnectionConfig.java
│           ├── AIDKey.java
│           └── KeyboardMapper.java
├── term3270-app/          # Swing UI application
│   └── src/main/java/
│       └── com.rhlowery.term3270/
│           ├── Main.java
│           ├── TerminalFrame.java
│           ├── TerminalPanel.java
│           ├── ConnectionDialog.java
│           └── FunctionKeyPanel.java
└── term3270-plugins/    # Future plugin modules
```

## 6. File Transfer Sequence (IND$FILE)

```mermaid
sequenceDiagram
    participant Host as Mainframe
    participant Parser as DataStreamParser
    participant Service as FileTransferService
    participant Session as ITerminalSession

    Session->>Host: IND$FILE GET dataset
    Host->>Parser: Read Partition (Query)
    Parser->>Session: Query Reply (Supports FT)
    Session->>Host: AID 0x88 (Query Reply)
    Host->>Parser: WSF (ID 0x47, Data Chunk)
    Parser->>Service: processFileData(byte)
    Service->>Service: Append to local file
    Host->>Parser: WSF (EOD)
    Parser->>Service: endField()
    Service->>Session: Notify Complete
```

## 7. Key Design Decisions

| Decision | Rationale |
|---|---|
| `ConnectionConfig` record | Immutable, thread-safe configuration object |
| `IDataStreamParser` interface | Enables 5250 protocol support |
| `EbcdicConverter` instance-based | Supports multi-codepage (Cp037, Cp285, Cp1140) |
| FlatLaf Dark L&F | Modern, professional UI appearance |
| OpenIDE Lookup | Service discovery without hard coupling |
