package com.rhlowery.term3270;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Low-level client for managing TN3270 Telnet communication and negotiation.
 * 
 * <p>This client implements the RFC 1576 (TN3270) and RFC 1041 (Telnet 3270 
 * Regime) standards. It handles IAC (Interpret As Command) sequences for 
 * option negotiation (Terminal Type, Binary Mode, End-of-Record) and provides 
 * transparent data transmission to the host.</p>
 * 
 * <p>Security is supported via SSL/TLS wrapping of the underlying socket when 
 * configured.</p>
 */
public class TelnetClient {

  private static final Logger logger = LoggerFactory.getLogger(TelnetClient.class);

  /**
   * Telnet Interpret As Command (IAC) byte.
   */
  private static final byte IAC = (byte) 0xFF;

  /**
   * Telnet DO command byte.
   */
  private static final byte DO = (byte) 0xFD;

  /**
   * Telnet WILL command byte.
   */
  private static final byte WILL = (byte) 0xFB;

  /**
   * Telnet WONT command byte.
   */
  private static final byte WONT = (byte) 0xFC;

  /**
   * Telnet DONT command byte.
   */
  private static final byte DONT = (byte) 0xFE;

  /**
   * Telnet Subnegotiation Begin (SB) byte.
   */
  private static final byte SB = (byte) 0xFA;

  /**
   * Telnet Subnegotiation End (SE) byte.
   */
  private static final byte SE = (byte) 0xF0;

  /**
   * Telnet End of Record (EOR) code.
   */
  private static final byte EOR_CODE = (byte) 0xEF;

  /**
   * Telnet Binary Transmission option.
   */
  private static final byte OPT_BINARY = (byte) 0x00;

  /**
   * Telnet End of Record option.
   */
  private static final byte OPT_EOR = (byte) 0x19;

  /**
   * Telnet Terminal Type option.
   */
  private static final byte OPT_TERM_TYPE = (byte) 0x18;

  /**
   * The underlying network socket.
   */
  private Socket socket;

  /**
   * Output stream for sending data to the host.
   */
  private OutputStream out;

  /**
   * The data stream parser for processing host data.
   */
  private IDataStreamParser parser;

  /**
   * Flag indicating if the reader thread is running.
   */
  private boolean running = false;

  /**
   * The terminal type advertised during negotiation.
   */
  private String terminalType = "IBM-3279-2-E";

  /**
   * Constructs a new Telnet client with the specified data stream parser.
   *
   * @param parser The parser to handle received 3270 data.
   */
  public TelnetClient(IDataStreamParser parser) {
    this.parser = parser;
  }

  /**
   * Sets the data stream parser for this client.
   *
   * @param parser The parser to use.
   */
  public void setParser(IDataStreamParser parser) {
    this.parser = parser;
  }

  /**
   * Establishes a connection to the host and starts the background reader thread.
   *
   * @param config The connection configuration (host, port, security settings).
   * @throws IOException If the connection cannot be established.
   */
  public void connect(ConnectionConfig config) throws IOException {
    this.terminalType = config.terminalType();
    
    if (config.secure()) {
      SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      SSLSocket sslSocket = (SSLSocket) factory.createSocket(config.host(), config.port());
      if (config.verifyHostname()) {
        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm("HTTPS");
        sslSocket.setSSLParameters(sslParams);
      }
      sslSocket.startHandshake();
      this.socket = sslSocket;
    } else {
      this.socket = new Socket(config.host(), config.port());
    }
    
    out = socket.getOutputStream();
    running = true;

    new Thread(this::readLoop, "TelnetReader").start();
  }

  /**
   * Gracefully closes the socket and stops the reader thread.
   *
   * @throws IOException If an error occurs during closure.
   */
  public void disconnect() throws IOException {
    running = false;
    if (socket != null) {
      socket.close();
    }
  }

  private void readLoop() {
    try (BufferedInputStream in = new BufferedInputStream(socket.getInputStream())) {
      while (running) {
        int b = in.read();
        if (b == -1) break;

        if ((byte) b == IAC) {
          int next = in.read();
          if (next == (0xFF & IAC)) {
            // Literal 0xFF
            parser.processByte(IAC);
          } else if (next != -1) {
            handleIac((byte) next, in);
          }
        } else {
          parser.processByte((byte) b);
        }
      }
    } catch (IOException e) {
      if (running) {
        logger.error("Error in Telnet reader loop", e);
      }
    }
  }

  private void handleIac(byte cmd, BufferedInputStream in) throws IOException {
    if (cmd == DO) {
      int opt = in.read();
      handleDo((byte) opt);
    } else if (cmd == WILL) {
      int opt = in.read();
      handleWill((byte) opt);
    } else if (cmd == SB) {
      handleSubnegotiation(in);
    } else if (cmd == EOR_CODE) {
      // End of record - notify parser if necessary
      parser.reset();
    }
  }

  private void handleDo(byte opt) throws IOException {
    logger.debug("Received DO 0x{}", Integer.toHexString(opt & 0xFF));
    if (opt == OPT_TERM_TYPE || opt == OPT_BINARY || opt == OPT_EOR) {
      sendIac(WILL, opt);
    } else {
      sendIac(WONT, opt);
    }
  }

  private void handleWill(byte opt) throws IOException {
    logger.debug("Received WILL 0x{}", Integer.toHexString(opt & 0xFF));
    if (opt == OPT_BINARY || opt == OPT_EOR) {
      sendIac(DO, opt);
    } else {
      sendIac(DONT, opt);
    }
  }

  private void handleSubnegotiation(BufferedInputStream in) throws IOException {
    int opt = in.read();
    if (opt == OPT_TERM_TYPE) {
      int subCmd = in.read(); // Should be SEND (0x01)
      if (subCmd == 0x01) {
        logger.info("Host requested terminal type identification");
        sendTermType();
      }
    }
    // Read until SE
    int b;
    while ((b = in.read()) != -1 && (byte) b != SE) ;
  }

  private void sendTermType() throws IOException {
    byte[] typeBytes = terminalType.getBytes();
    byte[] response = new byte[typeBytes.length + 6];
    response[0] = IAC;
    response[1] = SB;
    response[2] = OPT_TERM_TYPE;
    response[3] = 0x00; // IS
    System.arraycopy(typeBytes, 0, response, 4, typeBytes.length);
    response[response.length - 2] = IAC;
    response[response.length - 1] = SE;
    
    out.write(response);
    out.flush();
  }

  private void sendIac(byte cmd, byte opt) throws IOException {
    out.write(new byte[]{IAC, cmd, opt});
    out.flush();
  }

  /**
   * Sends a byte array to the host, escaping any IAC (0xFF) characters and 
   * appending the EOR (End of Record) sequence.
   *
   * @param data The raw data bytes to send.
   * @throws IOException If transmission fails.
   */
  public void sendData(byte[] data) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    for (byte b : data) {
      bos.write(b);
      if (b == IAC) {
        bos.write(IAC); // Escape IAC
      }
    }
    // Append IAC EOR
    bos.write(IAC);
    bos.write(EOR_CODE);
    
    out.write(bos.toByteArray());
    out.flush();
  }
}
