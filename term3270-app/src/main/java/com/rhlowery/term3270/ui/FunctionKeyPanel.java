package com.rhlowery.term3270.ui;

import com.rhlowery.term3270.AIDKey;
import com.rhlowery.term3270.ITerminalSession;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.openide.util.Lookup;

/**
 * A user interface component that provides visual buttons for 3270 function keys 
 * (PF1-PF12), attention keys (PA1-PA3, Enter, Clear), and editing operations 
 * (Insert, Delete, Erase EOF).
 * 
 * <p>The panel features a modern dark-mode aesthetic with grouped buttons, 
 * custom gradient backgrounds, and interactive hover effects. It interacts 
 * with the {@link ITerminalSession} via the NetBeans {@link Lookup} mechanism.</p>
 */
public class FunctionKeyPanel extends JPanel {

  /**
   * The dark background color for the main panel.
   */
  private static final Color DARK_BG = new Color(25, 25, 25);

  /**
   * The background color for button groups.
   */
  private static final Color GROUP_BG = new Color(35, 35, 35);

  /**
   * The default background color for buttons.
   */
  private static final Color BUTTON_BG = new Color(50, 50, 50);

  /**
   * The foreground color for button text.
   */
  private static final Color BUTTON_FG = Color.WHITE;

  /**
   * The color used for component borders.
   */
  private static final Color BORDER_COLOR = new Color(60, 60, 60);

  /**
   * The base color for Program Function (PF) key buttons.
   */
  private static final Color COLOR_PF = new Color(40, 60, 100);    // Indigo

  /**
   * The base color for Program Attention (PA) key buttons.
   */
  private static final Color COLOR_PA = new Color(120, 80, 20);   // Amber

  /**
   * The base color for reset and destructive operation buttons.
   */
  private static final Color COLOR_RESET = new Color(100, 30, 30); // Crimson

  /**
   * The base color for standard editing operation buttons.
   */
  private static final Color COLOR_EDIT = new Color(50, 50, 50);  // Slate

  /**
   * Initializes the function key panel with themed button groups.
   */
  public FunctionKeyPanel() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(DARK_BG);
    setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

    add(createEditingPanel());
    add(createPFPanel());
    add(createPAPanel());
    
    // Ensure the panel doesn't stretch vertically in a BorderLayout
    setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
  }

  private JPanel createEditingPanel() {
    JPanel panel = createGroupPanel("Editing");
    panel.add(new StyledButton("Reset", COLOR_RESET, e -> getSession().resetKeyboard()));
    panel.add(new StyledButton("Insert", COLOR_EDIT, e -> getSession().toggleInsertMode()));
    panel.add(new StyledButton("Delete", COLOR_EDIT, e -> getSession().deleteChar()));
    panel.add(new StyledButton("Erase EOF", COLOR_EDIT, e -> getSession().eraseEOF()));
    panel.add(new StyledButton("Erase Input", COLOR_RESET, e -> getSession().eraseInput()));
    return panel;
  }

  private JPanel createPFPanel() {
    JPanel panel = createGroupPanel("PF Keys");
    panel.setLayout(new GridLayout(2, 6, 4, 4));
    for (int i = 1; i <= 12; i++) {
      final int pf = i;
      panel.add(new StyledButton("PF" + i, COLOR_PF, e -> getSession().sendAID(AIDKey.valueOf("PF" + pf))));
    }
    return panel;
  }

  private JPanel createPAPanel() {
    JPanel panel = createGroupPanel("PA / System");
    panel.add(new StyledButton("Enter", COLOR_PA, e -> getSession().sendAID(AIDKey.ENTER)));
    panel.add(new StyledButton("Clear", COLOR_PA, e -> getSession().sendAID(AIDKey.CLEAR)));
    panel.add(new StyledButton("PA1", COLOR_PA, e -> getSession().sendAID(AIDKey.PA1)));
    panel.add(new StyledButton("PA2", COLOR_PA, e -> getSession().sendAID(AIDKey.PA2)));
    panel.add(new StyledButton("PA3", COLOR_PA, e -> getSession().sendAID(AIDKey.PA3)));
    return panel;
  }

  private JPanel createGroupPanel(String title) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    panel.setBackground(GROUP_BG);
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    TitledBorder border = BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(BORDER_COLOR), title);
    border.setTitleColor(Color.GRAY);
    panel.setBorder(border);
    return panel;
  }

  /**
   * Custom JButton with rounded corners and hover effects.
   */
  private static class StyledButton extends JButton {
    /**
     * Flag indicating whether the mouse is currently hovering over the button.
     */
    private boolean hover = false;

    /**
     * The default base color of the button.
     */
    private final Color baseColor;

    /**
     * Constructs a styled button with a label, color, and action listener.
     *
     * @param label     The button text.
     * @param baseColor The base background color.
     * @param listener  The action to perform on click.
     */
    public StyledButton(String label, Color baseColor, ActionListener listener) {
      super(label);
      this.baseColor = baseColor;
      addActionListener(listener);
      setFocusable(false);
      setContentAreaFilled(false);
      setBorderPainted(false);
      setForeground(BUTTON_FG);
      setPreferredSize(new Dimension(getPreferredSize().width + 10, 28));

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          hover = true;
          repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
          hover = false;
          repaint();
        }
      });
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      Color color1 = hover ? baseColor.brighter() : baseColor;
      Color color2 = color1.darker();
      
      GradientPaint gp = new GradientPaint(
          0, 0, color1, 0, getHeight(), color2);
      g2.setPaint(gp);

      g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
      g2.setColor(BORDER_COLOR);
      g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

      g2.dispose();
      super.paintComponent(g);
    }
  }

  /**
   * Discovers and returns the active terminal session from the application global 
   * lookup.
   *
   * @return The active ITerminalSession.
   */
  private ITerminalSession getSession() {
    return Lookup.getDefault().lookup(ITerminalSession.class);
  }
}
