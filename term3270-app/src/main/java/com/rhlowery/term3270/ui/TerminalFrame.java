package com.rhlowery.term3270.ui;

import com.rhlowery.term3270.ConnectionConfig;
import com.rhlowery.term3270.EbcdicConverter;
import com.rhlowery.term3270.ITerminalSession;
import com.rhlowery.term3270.IPrintProvider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main application window for the term3270 terminal emulator.
 * 
 * <p>This frame serves as the container for the {@link TerminalPanel} (rendering) 
 * and {@link FunctionKeyPanel} (interaction). It manages the main menu bar, 
 * handles user actions for connection, disconnection, printing, macros, and 
 * file transfers, and ensures graceful session cleanup on exit.</p>
 */
public class TerminalFrame extends JFrame {

  private static final Logger logger = LoggerFactory.getLogger(TerminalFrame.class);

  /** The last connected host address. */
  private String lastHost;
  /** The last connected port number. */
  private int lastPort;
  /** The last used terminal type string. */
  private String lastType;
  /** Whether the last connection was secure. */
  private boolean lastSecure;
  /** Whether hostname verification was enabled for the last connection. */
  private boolean lastVerifyHostname;
  /** The last used EBCDIC codepage. */
  private String lastCodepage = EbcdicConverter.DEFAULT_CODEPAGE;
  /** The last used emulation protocol (3270/5250). */
  private String lastEmulationType = "3270";

  /**
   * Constructs and initializes the main terminal frame, setting up the layout, 
   * menu bar, and child panels.
   */
  public TerminalFrame() {
    setTitle("term3270 Emulator");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        onExit();
      }
    });
    setLayout(new BorderLayout());
    getContentPane().setBackground(Color.BLACK);

    setJMenuBar(createMenuBar());

    TerminalPanel terminalPanel = new TerminalPanel();
    add(terminalPanel, BorderLayout.CENTER);

    FunctionKeyPanel functionKeyPanel = new FunctionKeyPanel();
    add(functionKeyPanel, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(null);
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    
    JMenu fileMenu = new JMenu("File");
    
    JMenuItem copyItem = new JMenuItem("Copy Screen to Clipboard");
    copyItem.addActionListener(e -> onCopyClipboard());
    fileMenu.add(copyItem);
    
    fileMenu.addSeparator();
    
    JMenuItem printTextItem = new JMenuItem("Print to Text...");
    printTextItem.addActionListener(e -> onPrintText());
    fileMenu.add(printTextItem);
    
    JMenuItem printPdfItem = new JMenuItem("Print to PDF...");
    printPdfItem.addActionListener(e -> onPrintPdf());
    fileMenu.add(printPdfItem);
    
    fileMenu.addSeparator();
    
    JMenuItem transferItem = new JMenuItem("File Transfer...");
    transferItem.addActionListener(e -> onFileTransfer());
    fileMenu.add(transferItem);
    
    fileMenu.addSeparator();
    
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(e -> onExit());
    fileMenu.add(exitItem);
    
    menuBar.add(fileMenu);

    JMenu sessionMenu = new JMenu("Session");

    JMenuItem connectItem = new JMenuItem("Connect...");
    connectItem.addActionListener(e -> onConnect());
    sessionMenu.add(connectItem);

    JMenuItem disconnectItem = new JMenuItem("Disconnect");
    disconnectItem.addActionListener(e -> onDisconnect());
    sessionMenu.add(disconnectItem);

    JMenuItem restartItem = new JMenuItem("Restart");
    restartItem.addActionListener(e -> onRestart());
    sessionMenu.add(restartItem);

    menuBar.add(sessionMenu);
    
    JMenu macroMenu = new JMenu("Macros");
    
    JMenuItem recordItem = new JMenuItem("Start Recording");
    recordItem.addActionListener(e -> onStartRecording());
    macroMenu.add(recordItem);
    
    JMenuItem stopItem = new JMenuItem("Stop Recording");
    stopItem.addActionListener(e -> onStopRecording());
    macroMenu.add(stopItem);
    
    macroMenu.addSeparator();
    
    JMenuItem playItem = new JMenuItem("Play Macro...");
    playItem.addActionListener(e -> onPlayMacro());
    macroMenu.add(playItem);
    
    menuBar.add(macroMenu);
    return menuBar;
  }

  /**
   * Displays the connection dialog and initiates a session if confirmed.
   */
  private void onConnect() {
    ConnectionDialog dialog = new ConnectionDialog(this);
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      lastHost = dialog.getHost();
      lastPort = dialog.getPort();
      lastType = dialog.getTerminalType();
      lastSecure = dialog.isSecure();
      lastVerifyHostname = dialog.isVerifyHostname();
      lastCodepage = dialog.getCodepage();
      lastEmulationType = dialog.getEmulationType();

      logger.info("Connecting to {}:{} using type {} and codepage {}", 
          lastHost, lastPort, lastType, lastCodepage);

      ITerminalSession session = Lookup.getDefault()
          .lookup(ITerminalSession.class);
      if (session != null) {
        session.connect(new ConnectionConfig(
            lastHost, lastPort, lastType,
            lastSecure, lastVerifyHostname,
            lastCodepage, lastEmulationType));
        logger.info("Session status: {}", session.getStatus());
      }
    }
  }

  /**
   * Disconnects the current active session.
   */
  private void onDisconnect() {
    ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
    if (session != null) {
      session.disconnect();
    }
  }

  /**
   * Restarts the session using the last known connection configuration.
   */
  private void onRestart() {
    onDisconnect();
    if (lastHost != null) {
      ITerminalSession session = Lookup.getDefault()
          .lookup(ITerminalSession.class);
      if (session != null) {
        session.connect(new ConnectionConfig(
            lastHost, lastPort, lastType,
            lastSecure, lastVerifyHostname,
            lastCodepage, lastEmulationType));
      }
    }
  }

  /**
   * Triggers the session-level clipboard copy operation.
   */
  private void onCopyClipboard() {
    ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
    if (session != null) {
      session.copyToClipboard();
    }
  }

  /**
   * Prompts the user for a file and saves the screen contents as plain text.
   */
  private void onPrintText() {
    JFileChooser chooser = new JFileChooser();
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
      if (session != null) {
        try (PrintWriter out = new PrintWriter(file)) {
          out.print(session.toPlainText());
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(this, 
              "Failed to save file: " + ex.getMessage());
        }
      }
    }
  }

  /**
   * Prompts the user for a file and exports the screen contents to PDF.
   */
  private void onPrintPdf() {
    JFileChooser chooser = new JFileChooser();
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
      if (session != null) {
        try {
          // Discover PDF print provider via Lookup
          boolean printed = false;
          for (IPrintProvider provider : Lookup.getDefault().lookupAll(IPrintProvider.class)) {
            if (provider.supports("PDF")) {
              provider.print(session.getScreenBuffer(), file);
              printed = true;
              break;
            }
          }
          if (!printed) {
            JOptionPane.showMessageDialog(this, "No PDF print provider found.");
          }
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, 
              "Failed to save PDF: " + ex.getMessage());
        }
      }
    }
  }

  /**
   * Initiates a macro recording session.
   */
  private void onStartRecording() {
    ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
    if (session != null) {
      session.startRecording();
      JOptionPane.showMessageDialog(this, "Recording started.");
    }
  }

  /**
   * Stops the current macro recording and prompts the user to save it as JSON.
   */
  private void onStopRecording() {
    ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
    if (session != null) {
      session.stopRecording();
      JFileChooser chooser = new JFileChooser();
      if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        try {
          session.getMacroManager().saveMacro(chooser.getSelectedFile());
          JOptionPane.showMessageDialog(this, "Macro saved.");
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, "Failed to save macro: " + ex.getMessage());
        }
      }
    }
  }

  /**
   * Prompts the user for a macro file and initiates playback.
   */
  private void onPlayMacro() {
    ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
    if (session != null) {
      JFileChooser chooser = new JFileChooser();
      if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        session.playMacro(chooser.getSelectedFile());
      }
    }
  }

  /**
   * Displays the file transfer dialog and starts an IND$FILE operation.
   */
  private void onFileTransfer() {
    FileTransferDialog dialog = new FileTransferDialog(this);
    dialog.setVisible(true);
    if (dialog.isApproved()) {
      ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
      if (session != null) {
        if (dialog.isDownload()) {
          session.downloadFile(dialog.getRemoteCommand(), dialog.getLocalPath());
        } else {
          session.uploadFile(dialog.getRemoteCommand(), dialog.getLocalPath());
        }
      }
    }
  }

  /**
   * Ensures all active sessions are disconnected before exiting the application.
   */
  private void onExit() {
    for (ITerminalSession session : Lookup.getDefault().lookupAll(ITerminalSession.class)) {
      if (session != null && !"DISCONNECTED".equals(session.getStatus())) {
        session.disconnect();
      }
    }
    System.exit(0);
  }
}
