# term3270 Utilities Module

Core protocol and terminal logic for the term3270 emulator.

## Architectural Overview

### C4 Component Diagram
```mermaid
graph TD
    Parser[DataStreamParser]
    Buffer[ScreenBuffer]
    Client[TelnetClient]
    Session[ITerminalSession]
    
    Session --> Parser
    Session --> Client
    Parser --> Buffer
    Client --> Parser
```

### Class Diagram (Protocol Layer)
```mermaid
classDiagram
    class IDataStreamParser {
        <<Interface>>
        +processByte(byte)
        +reset()
    }
    class DataStreamParser {
        -ParserState state
        -ScreenBuffer buffer
    }
    class ScreenBuffer {
        -ScreenCell[] cells
        +writeAtCba(char)
    }
    IDataStreamParser <|.. DataStreamParser
    DataStreamParser o-- ScreenBuffer
```

### Activity Diagram: Data Stream Processing
```mermaid
graph TD
    Start[Receive Byte] --> IAC{Is IAC?}
    IAC -- Yes --> Negotiate[Telnet Negotiation]
    IAC -- No --> Parse[DataStreamParser]
    Parse --> Command{Is Command?}
    Command -- Yes --> WCC[Handle WCC]
    Command -- No --> Order{Is Order?}
    Order -- Yes --> ProcessOrder[Execute SBA/SF/...]
    Order -- No --> Char[Write Character]
    ProcessOrder --> End[Update Screen]
    Char --> End
    WCC --> End
    Negotiate --> End
```

## Key Components
- **DataStreamParser**: Implements the 3270 state machine for command and order processing.
- **ScreenBuffer**: Manages the virtual terminal grid and field attributes.
- **TelnetClient**: Handles low-level TN3270 Telnet negotiation and security.
- **EbcdicConverter**: Bidirectional translation between EBCDIC and ASCII/Unicode.
