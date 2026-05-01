# Project Issues

| ID | Title                                       | Status | Branch         |
|----|---------------------------------------------|--------|----------------|
| 1  | Init Parent POM and CI/CD Infrastructure    | Closed | issue-1-init-..|
| 2  | Define Core Connection Behavior via BDD     | Closed | issue-2-conne..|
| 3  | Configure Executable JAR for term3270-app     | Closed | issue-3-execu..|
| 5  | Configure Comprehensive .gitignore          | Closed | issue-5-gitig..|
| 6  | Implement Modern Swing-based Terminal UI    | Closed | issue-6-swing..|
| 7  | Implement Keyboard Input and AID Handling   | Closed | issue-7-keybo..|
| 8  | Implement Virtual 24x80 Screen Buffer       | Closed | issue-8-screen..|
| 9  | Integrate Screen Buffer with Terminal UI    | Closed | issue-9-ui-int..|
| 10 | Implement 3270 Field Attributes Handling    | Closed | issue-10-field..|
| 11 | Implement TN3270 Data Stream Parser         | Closed | issue-11-proto..|
| 12 | Implement RA and EUA 3270 Orders            | Closed | issue-12-bulk..|
| 13 | Implement EBCDIC-to-ASCII Character Mapping | Closed | issue-13-ebcdic..|
| 14 | Implement TN3270 Telnet Connection Logic    | Closed | issue-14-netwo..|
| 15 | Implement Cursor Management and Tab Nav     | Closed | issue-15-cursor..|
| 16 | Implement 3270 Extended Attributes          | Closed | issue-16-exten..|
| 17 | Implement AID Response (Read Modified/Buffer)| Closed | issue-17-aid-..|
| 18 | Implement Field Validation and Formatting   | Closed | issue-18-valid..|
| 19 | Implement Insert/Delete and Screen Editing  | Closed | issue-19-editing|
| 20 | Implement Connection Management UI          | Closed | issue-20-conne..|
| 21 | Implement Cursor Arrow Nav and Backspace    | Closed | issue-21-nav    |
| 22 | Implement Raw Host Buffer Writes             | Closed | issue-22-raw    |
| 23 | Fix Extended Terminal Negotiation            | Closed | issue-23-neg    |
| 24 | Implement 3270 Color and Highlight Rendering | Closed | issue-24-color  |
| 25 | Implement Outbound IAC Escaping and EOR Framing | Closed | issue-25-iac    |
| 26 | Handle Keyboard Locking and Host State (WCC) | Closed | issue-26-wcc    |
| 27 | Implement Terminal Function Key Panel         | Closed | issue-27-buttons|
| 28 | Refine Function Key Button Labels            | Closed | issue-28-labels |
| 29 | Fix Button Contrast and Visibility          | Closed | issue-29-viz    |
| 30 | Implement Premium Aesthetic (Modern UI)     | Closed | issue-30-style  |
| 31 | Color-Coded Action Groups                   | Closed | issue-31-colors |
| 32 | Implement Standard OIA Indicators            | Closed | issue-32-oia    |
| 33 | Mouse-Click Cursor Positioning              | Closed | issue-33-mouse  |
| 34 | Audible and Visual Error State              | Closed | issue-34-error  |
| 35 | Fix Cursor Attribute Skipping Logic          | Closed | issue-35-cursor |
| 36 | Intelligent Focus & Auto-Recovery           | Closed | issue-36-focus  |
| 37 | Stacked Control Panels (Vertical Layout)    | Closed | issue-37-stack  |
| 38 | Support Secure TN3270 Connections (TN3270S) | Closed | issue-38-secure |
| 39 | Parameterize Screen Geometry (Models 3,4,5) | Closed | issue-39-geom   |
| 40 | Implement Multi-Codepage Support            | Closed | issue-40-lang   |
| 41 | Abstract Protocol for 5250 Compatibility    | Closed | issue-41-5250   |
| 42 | Modernize UI Look-and-Feel (FlatLaf)        | Closed | issue-42-ui     |
| 43 | Formalize Architectural Documentation       | Closed | issue-43-docs   |
| 44 | Implement Print Screen (Text & PDF)         | Closed | issue-44-print  |
| 45 | Macro Recording and Playback                | Closed | issue-45-macros |
| 46 | IND$FILE File Transfer Support              | Closed | issue-46-file   |
| 47 | Exit Menu with Auto-Disconnect              | Closed | issue-47-exit   |
| 48 | Select Emulation Type (3270/5250)           | Closed | issue-48-emu    |
| 49 | Select Terminal Size (rows x cols)          | Closed | issue-49-size   |
| 50 | Enterprise Readiness and Documentation      | Closed | issue-50-ent    |
| 51 | Implement IBM 5250 Protocol Support         | Closed | issue-51-5250   |
| 52 | Enable Diagram Rendering in Javadoc         | Closed | issue-52-diag   |
| 53 | Resolve Build Warnings and Refactor Packages| Closed | issue-53-warn   |
| 54 | Implement Pluggable Protocol Handlers       | Open   | issue-54-proto  |
| 55 | Implement Pluggable File Transfer Providers | Open   | issue-55-file   |
| 56 | Implement Pluggable Printing Engines        | Open   | issue-56-print  |
| 57 | Implement Pluggable Keyboard Layouts        | Open   | issue-57-kbd    |
| 58 | Implement Pluggable Macro Storage           | Open   | issue-58-macro  |
| 58 | Implement Pluggable Macro Storage           | Open   | issue-58-macro  |

## Issue 25: Implement Outbound IAC Escaping and EOR Framing
The current implementation of `sendData` sends raw bytes to the host. Any `0xFF` bytes in the 3270 data stream (common in buffer addresses) are not escaped as `0xFF 0xFF`, causing Telnet protocol errors. Additionally, outbound messages lack the mandatory `IAC EOR` (FF EF) termination, preventing the host from processing user input.

## Issue 26: Handle Keyboard Locking and Host State (WCC)
The terminal currently allows typing even when the host hasn't explicitly unlocked the keyboard (via the Write Control Character - WCC). This leads to out-of-sync interactions. We need to implement keyboard locking/unlocking and process the WCC correctly to ensure a "useable" session.

## Issue 27: Implement Terminal Function Key Panel
A dedicated UI panel (e.g., at the bottom or side of the terminal) containing buttons for standard 3270 operations that are not easily mapped to a modern PC keyboard, or to aid users who prefer a GUI. This includes:
- **AID Keys**: ENTER, CLEAR, PF1-PF24, PA1-PA3.
- **Editing Keys**: INSERT, DELETE, ERASE EOF, ERASE INPUT.
- **System Keys**: RESET (to unlock keyboard).

## Issue 28: Refine Function Key Button Labels
The button labels in the `FunctionKeyPanel` should be more descriptive and follow standard 3270 terminology. For example, `F1` should be `PF1`, `Ins` should be `Insert`, `EEOF` should be `Erase EOF`, etc. This will ensure that users can clearly identify each action.

## Issue 29: Fix Button Contrast and Visibility
On some platforms (especially macOS), standard Swing `JButton` components do not respect background colors correctly or use white text on white backgrounds by default. This makes the labels invisible. We need to implement a custom styling or look-and-feel adjustment to ensure high visibility and contrast.

## Issue 30: Implement Premium Aesthetic (Modern UI)
The current UI looks like a legacy Java application. We should update the styling to use rounded corners, gradients, and hover effects to create a more modern and professional experience.

## Issue 31: Color-Coded Action Groups
Group buttons by color to aid quick recognition: PF keys in blue, PA keys in yellow, and critical actions like Reset or Clear in red/amber.

## Issue 32: Implement Standard OIA Indicators
The status bar (Operator Information Area) needs to provide real-time feedback on the terminal state. This includes:
- **X SYSTEM / X WAIT**: Visible when the keyboard is locked by the host.
- **INSERT**: Visible when Insert Mode is active.
- **Cursor Coordinates**: Current row/column (already partially implemented, but needs professional styling).

## Issue 33: Mouse-Click Cursor Positioning
The user should be able to click on any character position in the terminal grid to move the cursor. This requires translating mouse coordinates $(x, y)$ into a 3270 buffer address based on font metrics.

## Issue 34: Audible and Visual Error State
When a user attempts an invalid action (e.g., typing into a protected field), the terminal should:
- **Lock the Keyboard**: Enter a "Keyboard Error" state that prevents further input.
- **Visual Feedback**: Show an error code in the OIA (e.g., `X - PROT` for protected).
- **Audible Feedback**: Play a "Beep" sound to alert the user.
- **Require Reset**: The user must click the `Reset` button (or press a shortcut) to clear the error and continue.

## Issue 36: Intelligent Focus & Auto-Recovery
The terminal should automatically guide the user to the nearest input field. If a user attempts to type while the cursor is in a protected area, the terminal should:
1. Scan forward for the next unprotected field.
2. Automatically jump the cursor to the first data position of that field.
3. Accept the typed character in the new position.
This prevents the frustrating `X-PROT` error when starting a session or clicking near a field.

## Issue 37: Stacked Control Panels (Vertical Layout)
The current horizontal arrangement of button groups (Editing, PF Keys, PA/System) extends the window width beyond the standard 80-column terminal grid. To maintain a compact and authentic terminal width, these groups should be stacked vertically at the bottom of the screen.

## Issue 38: Support Secure TN3270 Connections (TN3270S)
**Status:** Closed
The current TelnetClient uses plain-text sockets. We need to implement 
SSL/TLS support to allow secure connections to modern mainframe gateways. 
This should include options for truststore configuration and hostname 
verification.

## Issue 39: Parameterize Screen Geometry (Models 3, 4, 5)
**Status:** Closed
The ScreenBuffer and TerminalPanel are currently hardcoded to 24x80 (Model 2). 
We need to refactor these components to support dynamic dimensions:
- Model 3: 32 rows x 80 columns
- Model 4: 43 rows x 80 columns
- Model 5: 27 rows x 132 columns

## Issue 40: Implement Multi-Codepage Support
**Status:** Closed
Character mapping is currently limited to EBCDIC 037. We should implement 
a mechanism to support different EBCDIC codepages (e.g., UK 285, Euro 1140) 
based on user configuration.

## Issue 41: Abstract Protocol for 5250 Compatibility
**Status:** Closed
To support IBM i (AS/400) systems, the architecture should be abstracted 
to allow an IBM 5250 parser to be plugged in. This involves defining a 
common interface for terminal data stream parsers.

## Issue 42: Modernize UI Look-and-Feel (FlatLaf)
**Status:** Closed
Replace the legacy Swing Look-and-Feel with a modern alternative like 
FlatLaf. Add support for dark mode and configurable terminal fonts (e.g., 
IBM 3270 font, Cascadia Code).

## Issue 43: Formalize Architectural Documentation
**Status:** Closed
Create and maintain C4 and UML diagrams within the repository to document 
the system architecture, data flow, and component relationships as 
required by project standards.

## Issue 44: Implement Print Screen (Text & PDF)
**Status:** Closed
Users need a way to capture the current terminal screen. We should implement:
- **Print to Text**: Copy the buffer as a plain text file.
- **Print to PDF**: Generate a PDF document with the terminal font and 
  colors preserved.
- **Clipboard Integration**: Add a "Copy Screen" button to the UI.

## Issue 45: Macro Recording and Playback
**Status:** Closed
Implement a mechanism to record user keystrokes and play them back.
- **Storage**: Save macros as JSON files in the user's home directory.
- **UI**: Add "Record", "Stop", and "Play" buttons to the control panel.
- **Variable Support**: Allow simple variable injection (e.g., {{username}}) 
  into macros.

## Issue 46: IND$FILE File Transfer Support
**Status:** Closed
Implement the standard IBM IND$FILE protocol for transferring files between 
 the terminal and the mainframe. This involves:
- **Protocol**: Implementing the structured data stream for file chunks.
- **UI**: Adding a "File Transfer" dialog to select local files and target 
  datasets.

## Issue 47: Exit Menu with Auto-Disconnect
**Status:** Closed
Add an "Exit" menu item to the "File" menu.
- **Behavior**: Before exiting, the application should check all active 
  terminal sessions.
- **Cleanup**: If sessions are connected, it should automatically disconnect 
  them to prevent hung processes or orphaned connections on the host.
- **Exit**: Finally, terminate the Java process.

## Issue 48: Select Emulation Type (3270/5250)
**Status:** Closed
When opening a new connection, allow the user to select the emulation protocol.
- **Options**: Include "3270" (standard mainframe) and "5250" (AS/400).
- **Architecture**: Leverage the parser abstraction (Issue 41) to swap the 
  data stream parser based on this selection.

## Issue 49: Select Terminal Size (rows x cols)
**Status:** Closed
Provide a dropdown in the connection dialog to select the screen dimensions.
- **Options**: 
  - 80x24 (Model 2)
  - 80x32 (Model 3)
  - 80x43 (Model 4)
  - 132x27 (Model 5)
- **Impact**: Correctly initializes the ScreenBuffer and TerminalPanel font 
  scaling to fit the selected geometry.

## Issue 50: Enterprise Readiness and Documentation
**Status:** Closed
Transform the project into an enterprise-class application with high-quality 
automated reporting and maintainable architecture.
- **Logging**: Replace `System.out` with SLF4J/Logback.
- **Reporting**: Configure `mvn site` with:
  - Javadoc (Public API)
  - JaCoCo (Test Coverage)
  - Checkstyle/PMD (Code Quality)
  - Allure (Behavioral Test Reports)
- **Architecture**: 
  - Extract protocol constants to `ProtocolConstants`.
  - Refactor `DataStreamParser` for better extensibility.
  - Ensure up-to-date C4/UML diagrams in documentation.

## Issue 51: Implement IBM 5250 Protocol Support
**Status:** Closed
To support IBM i (AS/400) systems, the emulator requires a dedicated parser for the 5250 data stream.
- **Protocol**: Implementation of 5250-specific orders such as Write to Display (WTD) and Start of Field (SF).
- **Session**: Refactor `DefaultTerminalSession` to instantiate the appropriate parser based on user selection.
- **Testing**: New BDD feature files and step definitions for 5250 verification.

## Issue 52: Enable Diagram Rendering in Javadoc
**Status:** Closed
Enable automatic rendering of Mermaid and PlantUML diagrams within the 
generated Javadoc documentation using client-side scripts and custom doclets.

## Issue 53: Resolve Build Warnings and Refactor Packages
**Status:** Closed
Resolve all warnings reported during the `./mvnw site` build. This includes 
eliminating split packages by refactoring the `term3270-app` module, adding 
the `maven-jxr-plugin` for source cross-references, and fixing Javadoc 
syntax errors.

## Issue 54: Implement Pluggable Protocol Handlers
**Status:** Closed
Define a service provider interface (SPI) for `IDataStreamParser` using the NetBeans `Lookup` API. This will allow the core emulator to discover and load protocol implementations (e.g., 3270, 5250, VT100) dynamically from external JARs, improving modularity and extensibility.

## Issue 55: Implement Pluggable File Transfer Providers
**Status:** Closed
Refactor the file transfer logic into a pluggable system. While `IND$FILE` is the current standard, this feature will allow for other transfer protocols (Kermit, XMODEM, or proprietary host-specific protocols) to be added as separate modules without modifying the core `term3270-utilities` package.

## Issue 56: Implement Pluggable Printing Engines
**Status:** Closed
The current `PrintService` is hardcoded to PDF export via OpenPDF. This feature will introduce a plugin point for different export formats (HTML, CSV, plain text) or direct system printing drivers, allowing users to choose their preferred output method.

## Issue 57: Implement Pluggable Keyboard Layouts
**Status:** Closed
Move the `KeyboardMapper` logic into a pluggable system. This will enable users to create, share, and load custom keyboard maps for specific mainframe applications or different physical keyboard layouts (e.g., non-US layouts) via external configuration or modules.

## Issue 58: Implement Pluggable Macro Storage
**Status:** Closed
Enable pluggable macro storage backends. Currently, macros are stored as JSON files on the local disk. This feature will allow for alternative storage providers (e.g., SQLite databases, cloud storage, or encrypted containers) to be plugged in dynamically, by defining a macro persistence SPI.
