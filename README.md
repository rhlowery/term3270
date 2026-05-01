# term3270 Terminal Emulator

A modern, professional IBM 3270 terminal emulator built with Java and Swing.

## Project Structure

This project follows a multi-module Maven structure:
- **term3270-app**: Swing-based desktop application.
- **term3270-utilities**: Core protocol (TN3270) and terminal logic.
- **term3270-plugins**: Optional extension modules.

## Architecture

### C4 Context Diagram
```mermaid
graph TD
    User((User))
    Emulator[term3270 Emulator]
    Mainframe[Mainframe Host]
    FS[Local Filesystem]

    User -- "Interacts with UI" --> Emulator
    Emulator -- "TN3270/Telnet" --> Mainframe
    Emulator -- "IND$FILE / Macros" --> FS
```

### Module Dependencies
```mermaid
graph LR
    App[term3270-app]
    Utils[term3270-utilities]
    
    App --> Utils
```

### High-Level Class Diagram
```mermaid
classDiagram
    class ITerminalSession {
        <<Interface>>
        +connect(ConnectionConfig)
        +sendText(String)
        +sendAID(AIDKey)
    }
    class DefaultTerminalSession {
        -TelnetClient client
        -ScreenBuffer buffer
        -DataStreamParser parser
    }
    class TerminalFrame {
        -TerminalPanel panel
        -FunctionKeyPanel buttons
    }
    ITerminalSession <|.. DefaultTerminalSession
    TerminalFrame ..> ITerminalSession : uses
```

### Data Flow Overview
```mermaid
sequenceDiagram
    participant Host
    participant TelnetClient
    participant Parser
    participant Buffer
    participant UI

    Host->>TelnetClient: TN3270 Data
    TelnetClient->>Parser: Bytes
    Parser->>Buffer: Update Cells
    Buffer->>UI: Repaint
    UI->>Buffer: User Input
    Buffer->>TelnetClient: AID/Text Stream
    TelnetClient->>Host: Response
```

## Features
- Full TN3270 support (Model 2, 3, 4, 5).
- Extended attributes (Color, Highlight).
- Secure connections (TN3270S).
- IND$FILE transfer support.
- Macro recording and playback.
- Export screen to PDF.
- Dark theme powered by FlatLaf.

## Documentation
Comprehensive JavaDocs and architectural diagrams (PlantUML/Mermaid) are 
integrated directly into the source code via `package-info.java` files. 
Generate the full site using:
```bash
mvn site
```
The documentation will be available in `target/site/index.html`.
