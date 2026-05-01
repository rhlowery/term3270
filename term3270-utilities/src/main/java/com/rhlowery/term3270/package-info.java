/**
 * Core utility and protocol classes for the term3270 terminal emulator.
 * 
 * <p>This package contains the foundational components for 3270 emulation, 
 * including data stream parsing, EBCDIC character translation, screen buffer 
 * management, and network communication via Telnet.</p>
 * 
 * <h2>Architectural Overview</h2>
 * 
 * <h3>C4 Component Diagram</h3>
 * <pre>{@code
 * mermaid
 * componentDiagram
 *   package "com.rhlowery.term3270" {
 *     [ITerminalSession] <<Interface>>
 *     [DefaultTerminalSession] <<Component>>
 *     [DataStreamParser] <<Component>>
 *     [ScreenBuffer] <<Component>>
 *     [TelnetClient] <<Component>>
 *     
 *     [DefaultTerminalSession] ..> [IDataStreamParser]
 *     [DefaultTerminalSession] ..> [TelnetClient]
 *     [DataStreamParser] ..> [ScreenBuffer]
 *     [TelnetClient] ..> [IDataStreamParser]
 *   }
 * }</pre>
 * 
 * <h3>Data Flow Diagram</h3>
 * <pre>{@code
 * plantuml
 * @startuml
 * actor "Host" as Host
 * participant "TelnetClient" as TC
 * participant "DataStreamParser" as DSP
 * participant "ScreenBuffer" as SB
 * actor "User" as User
 * 
 * Host -> TC : Raw Bytes (TN3270)
 * TC -> DSP : Stream Bytes
 * DSP -> SB : Update Cells/Attributes
 * SB -> User : Rendered Screen
 * 
 * User -> SB : Keystrokes (Text/AID)
 * SB -> TC : Modified Data Stream
 * TC -> Host : TN3270 Packets
 * @enduml
 * }</pre>
 * 
 * <h3>Class Diagram</h3>
 * <pre>{@code
 * plantuml
 * @startuml
 * interface ITerminalSession {
 *   +connect(ConnectionConfig)
 *   +sendAID(AIDKey)
 *   +sendText(String)
 * }
 * 
 * class DefaultTerminalSession implements ITerminalSession
 * 
 * class ScreenBuffer {
 *   -ScreenCell[] cells
 *   +writeAtCba(char)
 *   +setAttribute(int, int, FieldAttribute)
 * }
 * 
 * class DataStreamParser {
 *   -ParserState state
 *   +processByte(byte)
 * }
 * 
 * class TelnetClient {
 *   +sendData(byte[])
 * }
 * 
 * DefaultTerminalSession *-- ScreenBuffer
 * DefaultTerminalSession *-- DataStreamParser
 * DefaultTerminalSession *-- TelnetClient
 * DataStreamParser o-- ScreenBuffer
 * @enduml
 * }</pre>
 * 
 * <h3>Activity Diagram: Protocol Parsing</h3>
 * <pre>{@code
 * plantuml
 * @startuml
 * start
 * :Receive Byte from Telnet;
 * if (Byte is IAC?) then (yes)
 *   :Handle Telnet Negotiation;
 * else (no)
 *   :Pass to DataStreamParser;
 *   if (State is COMMAND?) then (yes)
 *     :Identify 3270 Command;
 *     :Transition to WCC or DATA;
 *   else (no)
 *     :Process Order or Character;
 *     :Update ScreenBuffer;
 *   endif
 * endif
 * stop
 * @enduml
 * }</pre>
 */
package com.rhlowery.term3270;
