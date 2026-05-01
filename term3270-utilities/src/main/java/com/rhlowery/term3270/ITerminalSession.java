package com.rhlowery.term3270;

import java.io.File;

/**
 * Primary interface for managing a terminal emulator session.
 * 
 * <p>This interface provides methods for connecting to a host, sending user 
 * input (text and AID keys), navigating the cursor, and managing higher-level 
 * session features such as macros and file transfers.</p>
 */
public interface ITerminalSession {

  /**
   * Connects to the specified mainframe host using the provided configuration.
   *
   * @param config The connection configuration.
   */
  void connect(ConnectionConfig config);

  /**
   * Disconnects from the current host.
   */
  void disconnect();

  /**
   * Returns the current status of the session.
   *
   * @return The status string (e.g., "CONNECTED", "DISCONNECTED").
   */
  String getStatus();

  /**
   * Checks if the terminal screen is initialized.
   *
   * @return True if initialized, false otherwise.
   */
  boolean isScreenInitialized();

  /**
   * Sends an AID key to the mainframe.
   *
   * @param key The AID key to send.
   */
  void sendAID(AIDKey key);

  /**
   * Sends a string of text to the mainframe.
   *
   * @param text The text to send.
   */
  void sendText(String text);

  /**
   * Moves the cursor to the next unprotected field.
   */
  void tabForward();

  /**
   * Moves the cursor to the previous unprotected field.
   */
  void tabBackward();

  /**
   * Returns the current screen buffer.
   *
   * @return The screen buffer.
   */
  ScreenBuffer getScreenBuffer();

  /**
   * Returns whether the session is currently in Insert Mode.
   *
   * @return True if in insert mode.
   */
  boolean isInsertMode();

  /**
   * Toggles Insert Mode on or off.
   */
  void toggleInsertMode();

  /**
   * Deletes the character at the current cursor position.
   */
  void deleteChar();

  /**
   * Erases text from the cursor to the end of the unprotected field.
   */
  void eraseEOF();

  /**
   * Erases all unprotected input fields on the screen.
   */
  void eraseInput();

  /**
   * Moves the cursor one row up.
   */
  void cursorUp();

  /**
   * Moves the cursor one row down.
   */
  void cursorDown();

  /**
   * Moves the cursor one column left.
   */
  void cursorLeft();

  /**
   * Moves the cursor one column right.
   */
  void cursorRight();

  /**
   * Moves the cursor left and deletes the character at that position.
   */
  void backspace();

  /**
   * Returns the last data stream sent to the host.
   *
   * @return The last sent data.
   */
  byte[] getLastSentData();

  /**
   * Unlocks the keyboard manually (System Reset).
   */
  void resetKeyboard();

  /**
   * Returns whether the keyboard is in an error state.
   *
   * @return True if in an error state.
   */
  boolean isKeyboardError();

  /**
   * Returns the screen contents as plain text.
   *
   * @return The plain text screen.
   */
  String toPlainText();

  /**
   * Copies the screen contents to the system clipboard.
   */
  void copyToClipboard();

  /**
   * Sends raw data to the host.
   *
   * @param data The bytes to send.
   */
  void sendData(byte[] data);

  /**
   * Starts recording user actions for a macro.
   */
  void startRecording();

  /**
   * Stops the current macro recording.
   */
  void stopRecording();

  /**
   * Plays back a macro from a file.
   *
   * @param file The macro file to play.
   */
  void playMacro(File file);

  /**
   * Returns the macro manager instance for the session.
   *
   * @return The macro manager.
   */
  MacroManager getMacroManager();

  /**
   * Starts a file download from the mainframe.
   *
   * @param remoteCommand The IND$FILE command (e.g. "IND$FILE GET MY.DATA")
   * @param localPath     The local destination path.
   */
  void downloadFile(String remoteCommand, String localPath);

  /**
   * Starts a file upload to the mainframe.
   *
   * @param remoteCommand The IND$FILE command (e.g. "IND$FILE PUT MY.DATA")
   * @param localPath     The local source path.
   */
  void uploadFile(String remoteCommand, String localPath);

  /**
   * Returns the file transfer handler instance.
   *
   * @return The file transfer handler.
   */
  IFileTransferHandler getFileTransferHandler();

  /**
   * Simulates receiving data from the host.
   *
   * @param data The bytes received.
   */
  void receiveHostData(byte[] data);

  /**
   * Resets the session state (clears last sent data, etc.)
   */
  void reset();
}
