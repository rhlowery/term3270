package com.rhlowery.term3270;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation of a terminal session, managing the lifecycle of a 
 * TN3270 connection.
 * 
 * <p>This class coordinates between the {@link TelnetClient} for network I/O, 
 * the {@link DataStreamParser} for protocol handling, and the 
 * {@link ScreenBuffer} for state management. It also provides high-level 
 * features such as macro recording/playback and file transfer.</p>
 */
@ServiceProvider(service = ITerminalSession.class)
public class DefaultTerminalSession implements ITerminalSession {

  /**
   * The current connection status of the session.
   */
  private volatile String status = "DISCONNECTED";

  /**
   * Flag indicating if the terminal screen has been initialized.
   */
  private boolean screenInitialized = false;

  /**
   * Flag indicating if the terminal is in Insert Mode.
   */
  private boolean insertMode = false;

  /**
   * The core screen buffer instance for this session.
   */
  private final ScreenBuffer buffer = new ScreenBuffer();

  /**
   * The data stream parser instance for this session.
   */
  private IDataStreamParser parser;

  /**
   * The telnet client instance for network communication.
   */
  private final TelnetClient client = new TelnetClient(parser);

  /**
   * The macro manager instance for recording and playing macros.
   */
  private final MacroManager macroManager = new MacroManager();

  /**
   * The handler for file transfer operations.
   */
  private IFileTransferHandler fileTransferHandler;

  /**
   * The raw byte stream last sent to the host.
   */
  private byte[] lastSentData;

  /**
   * Initializes a new session with internal components configured.
   */
  public DefaultTerminalSession() {
    // Default to 3270 parser if available
    for (IParserProvider provider : Lookup.getDefault().lookupAll(IParserProvider.class)) {
      if (provider.supports("3270")) {
        this.parser = provider.createParser(buffer);
        break;
      }
    }
    
    // Default to IND$FILE transfer handler if available
    for (IFileTransferProvider provider : Lookup.getDefault().lookupAll(IFileTransferProvider.class)) {
      if (provider.supports("IND$FILE")) {
        this.fileTransferHandler = provider.createHandler();
        break;
      }
    }

    if (parser != null) {
      configureParser();
    }
  }

  private void configureParser() {
    parser.setReplyCallback(data -> {
      lastSentData = data;
      if (client != null) {
        try {
          client.sendData(data);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    parser.setFileTransferHandler(fileTransferHandler);
  }

  /**
   * Connects to the host using the provided configuration.
   *
   * @param config The connection parameters (host, port, terminal type, etc).
   */
  @Override
  public void connect(ConnectionConfig config) {
    try {
      // Discover and instantiate the appropriate parser via Lookup
      boolean parserFound = false;
      for (IParserProvider provider : Lookup.getDefault().lookupAll(IParserProvider.class)) {
        if (provider.supports(config.emulationType())) {
          parser = provider.createParser(buffer);
          parserFound = true;
          break;
        }
      }

      if (!parserFound) {
        throw new IOException("Unsupported emulation type: " + config.emulationType());
      }

      configureParser();
      client.setParser(parser);

      
      
      EbcdicConverter conv =
          new EbcdicConverter(config.codepage());
      buffer.setConverter(conv);
      parser.setConverter(conv);
      buffer.resize(config.rows(), config.cols());
      client.connect(config);
      status = "CONNECTED";
      screenInitialized = true;
      buffer.setKeyboardLocked(false);
    } catch (Exception e) {
      status = "CONNECTION_FAILED (" + e.getMessage() + ")";
      e.printStackTrace();
    }
  }

  @Override
  public void disconnect() {
    try {
      client.disconnect();
      status = "DISCONNECTED";
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getStatus() {
    return status;
  }

  @Override
  public boolean isScreenInitialized() {
    return screenInitialized;
  }

  /**
   * Sends an AID key to the host, including the cursor position and modified fields.
   *
   * @param key The AID key to send (e.g., ENTER, PF1).
   */
  @Override
  public void sendAID(AIDKey key) {
    if (buffer.isKeyboardLocked()) return;
    
    if (macroManager.isRecording()) {
      macroManager.addAction(new MacroAction(
          MacroAction.ActionType.AID, key.name()));
    }
    
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.write(key.getCode());
      
      byte[] cursorAddr = AddressConverter.fromAddress(buffer.getCursorAddress());
      out.write(cursorAddr[0]);
      out.write(cursorAddr[1]);
      
      if (key != AIDKey.PA1 && key != AIDKey.PA2 && key != AIDKey.PA3 && key != AIDKey.CLEAR) {
        out.write(buffer.readModified());
      }
      
      this.lastSentData = out.toByteArray();
      if (status.equals("CONNECTED")) {
        buffer.setKeyboardLocked(true);
        client.sendData(lastSentData);
      }
      parser.reset();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the last raw data stream sent to the host.
   *
   * @return The last sent byte array.
   */
  @Override
  public byte[] getLastSentData() {
    return lastSentData;
  }

  /**
   * Sends a string of text to the current cursor position in the buffer.
   *
   * @param text The text to send.
   */
  @Override
  public void sendText(String text) {
    if (buffer.isKeyboardLocked()) return;
    
    if (macroManager.isRecording()) {
      macroManager.addAction(new MacroAction(
          MacroAction.ActionType.TEXT, text));
    }
    
    // Actually write text to the buffer
    for (int i = 0; i < text.length(); i++) {
      if (insertMode) {
        buffer.insertAtCursor(text.charAt(i));
      } else {
        buffer.writeAtCba(text.charAt(i));
      }
    }
  }

  /**
   * Returns whether the session is currently in Insert Mode.
   *
   * @return True if in insert mode.
   */
  @Override
  public boolean isInsertMode() {
    return insertMode;
  }

  /**
   * Toggles between Insert and Overwrite modes.
   */
  @Override
  public void toggleInsertMode() {
    insertMode = !insertMode;
  }

  /**
   * Deletes the character at the current cursor position and shifts the 
   * field contents left.
   */
  @Override
  public void deleteChar() {
    if (buffer.isKeyboardLocked()) return;
    buffer.deleteAtCursor();
  }

  /**
   * Erases all characters from the cursor to the end of the current field.
   */
  @Override
  public void eraseEOF() {
    if (buffer.isKeyboardLocked()) return;
    buffer.eraseEndOfField();
  }

  /**
   * Erases all unprotected input fields on the screen and resets their 
   * Modified Data Tags (MDT).
   */
  @Override
  public void eraseInput() {
    if (buffer.isKeyboardLocked()) return;
    buffer.eraseInput();
  }

  /**
   * Moves the cursor up one row, wrapping if necessary.
   */
  @Override
  public void cursorUp() {
    int current = buffer.getCursorAddress();
    int size = buffer.getRows() * buffer.getCols();
    int next = (current - buffer.getCols() + size) % size;
    buffer.setCursorAddress(next);
    buffer.setCba(next);
  }

  /**
   * Moves the cursor down one row, wrapping if necessary.
   */
  @Override
  public void cursorDown() {
    int current = buffer.getCursorAddress();
    int size = buffer.getRows() * buffer.getCols();
    int next = (current + buffer.getCols()) % size;
    buffer.setCursorAddress(next);
    buffer.setCba(next);
  }

  /**
   * Moves the cursor left one character, wrapping if necessary.
   */
  @Override
  public void cursorLeft() {
    int current = buffer.getCursorAddress();
    int size = buffer.getRows() * buffer.getCols();
    int next = (current - 1 + size) % size;
    buffer.setCursorAddress(next);
    buffer.setCba(next);
  }

  /**
   * Moves the cursor right one character, wrapping if necessary.
   */
  @Override
  public void cursorRight() {
    int current = buffer.getCursorAddress();
    int size = buffer.getRows() * buffer.getCols();
    int next = (current + 1) % size;
    buffer.setCursorAddress(next);
    buffer.setCba(next);
  }

  /**
   * Moves the cursor left and deletes the character at that position.
   */
  @Override
  public void backspace() {
    if (buffer.isKeyboardLocked()) return;
    cursorLeft();
    deleteChar();
  }

  /**
   * Moves the cursor forward to the start of the next unprotected field.
   */
  @Override
  public void tabForward() {
    int next = buffer.findNextUnprotected(buffer.getCursorAddress());
    buffer.setCursorAddress(next);
    buffer.setCba(next);
  }

  /**
   * Moves the cursor backward to the start of the current or previous 
   * unprotected field.
   */
  @Override
  public void tabBackward() {
    int next = buffer.findPreviousUnprotected(buffer.getCursorAddress());
    buffer.setCursorAddress(next);
    buffer.setCba(next);
  }

  /**
   * Returns the underlying screen buffer instance.
   *
   * @return The screen buffer.
   */
  @Override
  public ScreenBuffer getScreenBuffer() {
    return buffer;
  }

  /**
   * Manually unlocks the keyboard (System Reset).
   */
  @Override
  public void resetKeyboard() {
    buffer.setKeyboardLocked(false);
  }

  /**
   * Returns whether the keyboard is in a logical error state (e.g., trying 
   * to type in a protected field).
   *
   * @return True if in an error state.
   */
  @Override
  public boolean isKeyboardError() {
    return buffer.isKeyboardError();
  }

  /**
   * Translates the entire screen buffer into a plain text representation.
   *
   * @return The screen as a String.
   */
  @Override
  public String toPlainText() {
    return buffer.toPlainText();
  }

  /**
   * Copies the plain text screen contents to the system clipboard.
   */
  @Override
  public void copyToClipboard() {
    String text = toPlainText();
    StringSelection selection = 
        new StringSelection(text);
    Toolkit.getDefaultToolkit().getSystemClipboard()
        .setContents(selection, null);
  }

  /**
   * Sends raw protocol bytes to the host via the telnet client.
   *
   * @param data The bytes to send.
   */
  @Override
  public void sendData(byte[] data) {
    lastSentData = data;
    if (client != null) {
      try {
        client.sendData(data);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void startRecording() {
    macroManager.startRecording();
  }

  @Override
  public void stopRecording() {
    macroManager.stopRecording();
  }

  /**
   * Plays back a macro sequence from the specified JSON file.
   *
   * @param file The macro JSON file.
   */
  @Override
  public void playMacro(File file) {
    try {
      List<MacroAction> actions = macroManager.loadMacro(file);
      macroManager.play(this, actions);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the macro manager instance for the session.
   *
   * @return The macro manager.
   */
  @Override
  public MacroManager getMacroManager() {
    return macroManager;
  }

  /**
   * Initiates an IND$FILE download from the mainframe.
   *
   * @param remoteCommand The IND$FILE GET command.
   * @param localPath     The local destination path.
   */
  @Override
  public void downloadFile(String remoteCommand, String localPath) {
    try {
      fileTransferHandler.startDownload(localPath);
      sendText(remoteCommand);
      sendAID(AIDKey.ENTER);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Initiates an IND$FILE upload to the mainframe.
   *
   * @param remoteCommand The IND$FILE PUT command.
   * @param localPath     The local source path.
   */
  @Override
  public void uploadFile(String remoteCommand, String localPath) {
    try {
      sendText(remoteCommand);
      sendAID(AIDKey.ENTER);
      // In a real scenario, we would wait for the host's 'Go' structured field
      // For now, we'll just start sending the data
      fileTransferHandler.startUpload(localPath, this);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the file transfer handler instance for the session.
   *
   * @return The file transfer handler.
   */
  @Override
  public IFileTransferHandler getFileTransferHandler() {
    return fileTransferHandler;
  }

  /**
   * Simulates or handles receiving raw protocol data from the host.
   *
   * @param data The byte array to process.
   */
  @Override
  public void receiveHostData(byte[] data) {
    for (byte b : data) {
      parser.processByte(b);
    }
  }

  /**
   * Resets the session state and clears the screen buffer.
   */
  @Override
  public void reset() {
    lastSentData = null;
    buffer.clear();
    parser.reset();
  }
}
