package com.rhlowery.term3270.ui;

import com.rhlowery.term3270.ITerminalSession;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openide.util.Lookup;

/**
 * Primary entry point for the term3270 terminal emulator application.
 * 
 * <p>This class handles the initial application bootstrap, including installing 
 * the modern FlatLaf dark theme, launching the main {@link TerminalFrame} on 
 * the Event Dispatch Thread (EDT), and discovering the active session 
 * implementation using the OpenIDE {@link Lookup} service.</p>
 */
public class Main {
  /**
   * Application-wide logger instance.
   */
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  /**
   * Application entry point.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    // Install modern FlatLaf Dark Look and Feel
    FlatDarkLaf.setup();

    SwingUtilities.invokeLater(() -> {
      TerminalFrame frame = new TerminalFrame();
      frame.setVisible(true);

      logger.info("Starting term3270 Emulator UI...");

      // Discover the session implementation via OpenIDE Lookup
      ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);

      if (session != null) {
        logger.info("Session status: {}", session.getStatus());
      } else {
        logger.warn("No ITerminalSession implementation found.");
      }
    });
  }
}
