/**
 * UI components for the term3270 terminal emulator application.
 * 
 * <p>This package contains the Swing-based user interface, including the main 
 * window {@link TerminalFrame}, the rendering canvas {@link TerminalPanel}, 
 * and various dialogs for connection and file transfer management.</p>
 * 
 * <h2>Architectural Overview</h2>
 * 
 * <h3>C4 Container Diagram</h3>
 * <pre>{@code
 * mermaid
 * containerDiagram
 *   System_Boundary(term3270, "term3270 Emulator") {
 *     Container(app, "term3270-app", "Java Swing", "Main user interface and event handling")
 *     Container(utils, "term3270-utilities", "Java", "Protocol parsing and buffer management")
 *     
 *     app -> utils : Uses ITerminalSession
 *   }
 *   System_Ext(host, "Mainframe Host", "z/OS / VSE / VM")
 *   utils -> host : TN3270 Protocol
 * }</pre>
 * 
 * <h3>Class Diagram</h3>
 * <pre>{@code
 * plantuml
 * @startuml
 * class TerminalFrame extends JFrame {
 *   -TerminalPanel terminalPanel
 *   -FunctionKeyPanel functionKeyPanel
 *   +onConnect()
 *   +onExit()
 * }
 * 
 * class TerminalPanel extends JPanel {
 *   -ScreenBuffer buffer
 *   +paintComponent(Graphics)
 * }
 * 
 * class FunctionKeyPanel extends JPanel {
 *   +createPFPanel()
 *   +createPAPanel()
 * }
 * 
 * TerminalFrame *-- TerminalPanel
 * TerminalFrame *-- FunctionKeyPanel
 * @enduml
 * }</pre>
 * 
 * <h3>Data Flow Diagram</h3>
 * <pre>{@code
 * plantuml
 * @startuml
 * actor User
 * participant "TerminalPanel" as TP
 * participant "TerminalFrame" as TF
 * participant "ITerminalSession" as Session
 * 
 * User -> TP : Keystrokes
 * TP -> Session : sendText() / sendAID()
 * Session -> TP : repaint()
 * TF -> Session : connect() / disconnect()
 * @enduml
 * }</pre>
 * 
 * <h3>Activity Diagram: Connection Flow</h3>
 * <pre>{@code
 * plantuml
 * @startuml
 * :User Clicks Connect;
 * :Show ConnectionDialog;
 * if (User Confirms?) then (yes)
 *   :Initialize ConnectionConfig;
 *   :Call session.connect();
 *   :Start Network Thread;
 *   :Refresh TerminalPanel;
 * else (no)
 *   :Close Dialog;
 * endif
 * stop
 * @enduml
 * }</pre>
 */
package com.rhlowery.term3270.ui;
