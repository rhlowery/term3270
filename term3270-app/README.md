# term3270 Application Module

The Swing-based user interface for the term3270 emulator.

## Architectural Overview

### C4 Container Diagram
```mermaid
graph TD
    UI[term3270-app]
    Utils[term3270-utilities]
    User((User))
    Host[Mainframe]

    User --> UI
    UI -- "ITerminalSession" --> Utils
    Utils -- "TN3270" --> Host
```

### Class Diagram (UI Layer)
```mermaid
classDiagram
    class TerminalFrame {
        -TerminalPanel panel
        -FunctionKeyPanel buttons
        +onConnect()
    }
    class TerminalPanel {
        -ScreenBuffer buffer
        +paintComponent(Graphics)
    }
    class ConnectionDialog {
        +getHost()
        +getPort()
    }
    TerminalFrame *-- TerminalPanel
    TerminalFrame *-- FunctionKeyPanel
    TerminalFrame ..> ConnectionDialog : uses
```

### Activity Diagram: User Interaction Flow
```mermaid
graph TD
    Start[User Input] --> Key{Key Pressed?}
    Key -- AID --> AID[Send AID to Host]
    Key -- Text --> Text[Update Buffer]
    AID --> Wait[Wait for Host Response]
    Wait --> Update[Repaint Screen]
    Text --> Update
    Update --> End[Rendered Frame]
```

## Key UI Components
- **TerminalFrame**: The main application window and menu system.
- **TerminalPanel**: Custom rendering engine for the terminal screen.
- **FunctionKeyPanel**: Modern PF key and action button interface.
- **ConnectionDialog**: UI for session configuration.
