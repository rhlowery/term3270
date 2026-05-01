package com.rhlowery.term3270.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * A modal dialog that allows the user to configure and initiate a new terminal 
 * session.
 * 
 * <p>This dialog provides input fields for the target host, port, terminal type, 
 * security settings (SSL/TLS), character encoding (codepage), and emulation 
 * protocol (3270 or 5250). It also maps common screen sizes to their 
 * respective IBM terminal model strings.</p>
 */
public class ConnectionDialog extends JDialog {

  /** Input field for the host address. */
  private JTextField hostField;
  /** Input field for the connection port. */
  private JTextField portField;
  /** Input field for the IBM terminal type string. */
  private JTextField typeField;
  /** Dropdown for selecting the character encoding (codepage). */
  private JComboBox<String> codepageCombo;
  /** Dropdown for selecting the emulation protocol (3270/5250). */
  private JComboBox<String> emulationCombo;
  /** Dropdown for selecting the terminal screen size. */
  private JComboBox<String> sizeCombo;
  /** Checkbox to enable SSL/TLS security. */
  private JCheckBox secureCheckBox;
  /** Checkbox to enable hostname verification for secure connections. */
  private JCheckBox verifyHostnameCheckBox;
  /** Flag indicating if the user confirmed the dialog. */
  private boolean confirmed = false;

  /**
   * Constructs a new connection dialog.
   *
   * @param parent The parent frame for modal behavior.
   */
  public ConnectionDialog(JFrame parent) {
    super(parent, "Connect to Host", true);
    setLayout(new BorderLayout());

    JPanel mainPanel = new JPanel(
        new GridLayout(8, 2, 5, 5));
    mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    mainPanel.add(new JLabel("Hostname:"));
    hostField = new JTextField("localhost", 20);
    mainPanel.add(hostField);

    mainPanel.add(new JLabel("Port:"));
    portField = new JTextField("3270", 10);
    mainPanel.add(portField);

    mainPanel.add(new JLabel("Terminal Type:"));
    typeField = new JTextField("IBM-3279-2-E", 15);
    mainPanel.add(typeField);

    mainPanel.add(new JLabel("Secure (TN3270S):"));
    secureCheckBox = new JCheckBox();
    mainPanel.add(secureCheckBox);

    mainPanel.add(new JLabel("Verify Hostname:"));
    verifyHostnameCheckBox = new JCheckBox();
    verifyHostnameCheckBox.setEnabled(false);
    secureCheckBox.addActionListener(e ->
        verifyHostnameCheckBox.setEnabled(
            secureCheckBox.isSelected()));
    mainPanel.add(verifyHostnameCheckBox);

    mainPanel.add(new JLabel("Codepage:"));
    codepageCombo = new JComboBox<>(new String[]{
        "Cp037", "Cp285", "Cp1140",
        "Cp273", "Cp277", "Cp278",
        "Cp280", "Cp284", "Cp297",
        "Cp500", "Cp871"
    });
    codepageCombo.setSelectedItem("Cp037");
    mainPanel.add(codepageCombo);

    mainPanel.add(new JLabel("Emulation Type:"));
    emulationCombo = new JComboBox<>(new String[]{"3270", "5250"});
    mainPanel.add(emulationCombo);

    mainPanel.add(new JLabel("Terminal Size:"));
    sizeCombo = new JComboBox<>(new String[]{"80x24", "80x32", "80x43", "132x27"});
    sizeCombo.addActionListener(e -> {
      String selected = (String) sizeCombo.getSelectedItem();
      if ("80x24".equals(selected)) typeField.setText("IBM-3278-2");
      else if ("80x32".equals(selected)) typeField.setText("IBM-3278-3");
      else if ("80x43".equals(selected)) typeField.setText("IBM-3278-4");
      else if ("132x27".equals(selected)) typeField.setText("IBM-3278-5");
    });
    mainPanel.add(sizeCombo);

    add(mainPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton connectButton = new JButton("Connect");
    connectButton.addActionListener(e -> {
      confirmed = true;
      setVisible(false);
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> setVisible(false));

    buttonPanel.add(connectButton);
    buttonPanel.add(cancelButton);
    add(buttonPanel, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(parent);
  }

  /**
   * Checks if the user confirmed the dialog by clicking "Connect".
   *
   * @return True if confirmed.
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Returns the target host entered by the user.
   *
   * @return The hostname or IP.
   */
  public String getHost() {
    return hostField.getText();
  }

  /**
   * Returns the target TCP port entered by the user.
   *
   * @return The port number.
   */
  public int getPort() {
    try {
      return Integer.parseInt(portField.getText());
    } catch (NumberFormatException e) {
      return 3270;
    }
  }

  /**
   * Returns the IBM terminal model string (e.g., IBM-3278-2).
   *
   * @return The terminal type.
   */
  public String getTerminalType() {
    return typeField.getText();
  }

  /**
   * Checks if the user enabled SSL/TLS security.
   *
   * @return True if secure.
   */
  public boolean isSecure() {
    return secureCheckBox.isSelected();
  }

  /**
   * Checks if the user enabled hostname verification for secure connections.
   *
   * @return True if verification is enabled.
   */
  public boolean isVerifyHostname() {
    return verifyHostnameCheckBox.isSelected();
  }

  /**
   * Returns the selected EBCDIC codepage.
   *
   * @return The codepage name (e.g., "Cp037").
   */
  public String getCodepage() {
    return (String) codepageCombo.getSelectedItem();
  }

  /**
   * Returns the selected emulation protocol.
   *
   * @return The emulation type string (e.g., "3270").
   */
  public String getEmulationType() {
    return (String) emulationCombo.getSelectedItem();
  }
}
