package com.rhlowery.term3270.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A modal dialog for configuring and initiating IND$FILE file transfers.
 * 
 * <p>This dialog allows the user to select between upload (PUT) and 
 * download (GET) modes, specify the host command (e.g., "IND$FILE GET MY.DATA"), 
 * and select the local source or destination file using a file chooser.</p>
 */
public class FileTransferDialog extends JDialog {
  /** Dropdown for selecting transfer mode (GET/PUT). */
  private final JComboBox<String> modeBox = new JComboBox<>(new String[]{"Download (GET)", "Upload (PUT)"});
  /** Input field for the host-side IND$FILE command. */
  private final JTextField commandField = new JTextField("IND$FILE GET MY.DATA");
  /** Input field for the local file system path. */
  private final JTextField pathField = new JTextField();
  /** Flag indicating if the user approved the transfer. */
  private boolean approved = false;

  /**
   * Constructs a new file transfer dialog.
   *
   * @param parent The parent frame for modal behavior.
   */
  public FileTransferDialog(Frame parent) {
    super(parent, "File Transfer", true);
    setLayout(new BorderLayout());

    JPanel center = new JPanel(new GridLayout(3, 2, 5, 5));
    center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    center.add(new JLabel("Mode:"));
    modeBox.addActionListener(e -> {
      if (modeBox.getSelectedIndex() == 0) {
        commandField.setText("IND$FILE GET MY.DATA");
      } else {
        commandField.setText("IND$FILE PUT MY.DATA");
      }
    });
    center.add(modeBox);

    center.add(new JLabel("Remote Command:"));
    center.add(commandField);

    center.add(new JLabel("Local File:"));
    JPanel pathPanel = new JPanel(new BorderLayout());
    pathPanel.add(pathField, BorderLayout.CENTER);
    JButton browseBtn = new JButton("...");
    browseBtn.addActionListener(e -> onBrowse());
    pathPanel.add(browseBtn, BorderLayout.EAST);
    center.add(pathPanel);

    add(center, BorderLayout.CENTER);

    JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton okBtn = new JButton("Transfer");
    okBtn.addActionListener(e -> {
      approved = true;
      setVisible(false);
    });
    JButton cancelBtn = new JButton("Cancel");
    cancelBtn.addActionListener(e -> setVisible(false));
    south.add(okBtn);
    south.add(cancelBtn);
    add(south, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(parent);
  }

  private void onBrowse() {
    JFileChooser chooser = new JFileChooser();
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      pathField.setText(chooser.getSelectedFile().getAbsolutePath());
    }
  }

  /**
   * Checks if the user approved the transfer by clicking "Transfer".
   *
   * @return True if approved.
   */
  public boolean isApproved() {
    return approved;
  }

  /**
   * Checks if the selected mode is Download (GET).
   *
   * @return True if download, false if upload.
   */
  public boolean isDownload() {
    return modeBox.getSelectedIndex() == 0;
  }

  /**
   * Returns the IND$FILE command entered by the user (e.g., "IND$FILE GET MY.DATA").
   *
   * @return The remote command.
   */
  public String getRemoteCommand() {
    return commandField.getText();
  }

  /**
   * Returns the local file path for the source or destination.
   *
   * @return The local absolute path.
   */
  public String getLocalPath() {
    return pathField.getText();
  }
}
