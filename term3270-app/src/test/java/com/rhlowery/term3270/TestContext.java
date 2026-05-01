package com.rhlowery.term3270;

import org.openide.util.Lookup;

/**
 * Shared context for Cucumber steps.
 */
public class TestContext {
  /**
   * The screen buffer instance for the current test.
   */
  private ScreenBuffer buffer;

  /**
   * The data stream parser instance for the current test.
   */
  private IDataStreamParser parser;

  /**
   * The terminal session instance for the current test.
   */
  private ITerminalSession session;

  /**
   * The port used by a mock server in connection tests.
   */
  private int mockPort;

  /**
   * Gets the mock port number.
   *
   * @return The mock port.
   */
  public int getMockPort() {
    return mockPort;
  }

  /**
   * Sets the mock port number.
   *
   * @param mockPort The port to set.
   */
  public void setMockPort(int mockPort) {
    this.mockPort = mockPort;
  }

  /**
   * Gets the screen buffer, initializing it from the session if necessary.
   *
   * @return The screen buffer.
   */
  public ScreenBuffer getBuffer() {
    if (buffer == null) {
      ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
      if (session != null) {
        buffer = session.getScreenBuffer();
      } else {
        buffer = new ScreenBuffer();
      }
    }
    return buffer;
  }

  /**
   * Sets the screen buffer and initializes a corresponding parser.
   *
   * @param buffer The screen buffer to set.
   */
  public void setBuffer(ScreenBuffer buffer) {
    this.buffer = buffer;
    this.parser = new DataStreamParser(buffer);
  }

  /**
   * Gets the data stream parser, initializing it if necessary.
   *
   * @return The data stream parser.
   */
  public IDataStreamParser getParser() {
    if (parser == null) {
      parser = new DataStreamParser(getBuffer());
    }
    return parser;
  }

  /**
   * Sets the data stream parser.
   *
   * @param parser The parser to set.
   */
  public void setParser(IDataStreamParser parser) {
    this.parser = parser;
  }

  /**
   * Gets the EBCDIC converter from the buffer.
   *
   * @return The converter.
   */
  public EbcdicConverter getConverter() {
    // Note: ScreenBuffer should probably expose converter or we use default
    return EbcdicConverter.defaultConverter();
  }

  /**
   * Gets the terminal session from the default lookup.
   *
   * @return The terminal session implementation.
   */
  public ITerminalSession getSession() {
    if (session == null) {
      session = Lookup.getDefault().lookup(ITerminalSession.class);
    }
    return session;
  }
}
