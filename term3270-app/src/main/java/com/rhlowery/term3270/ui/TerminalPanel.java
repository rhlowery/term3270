package com.rhlowery.term3270.ui;

import com.rhlowery.term3270.AIDKey;
import com.rhlowery.term3270.FieldAttribute;
import com.rhlowery.term3270.ITerminalSession;
import com.rhlowery.term3270.ScreenBuffer;
import com.rhlowery.term3270.ScreenCell;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.openide.util.Lookup;

/**
 * The primary display component for the terminal emulator.
 * 
 * <p>TerminalPanel is responsible for rendering the character grid and attributes 
 * from the {@link ScreenBuffer} onto a Swing component using monospaced fonts. 
 * It also handles keyboard and mouse input, translating them into operations on 
 * the {@link ITerminalSession}.</p>
 * 
 * <p>The panel includes an Operator Information Area (OIA) at the bottom to 
 * display connection status, cursor position, and keyboard lock indicators 
 * (e.g., "X SYSTEM", "X - PROT").</p>
 */
public class TerminalPanel extends JPanel {

  /** The number of rows in the terminal display. */
  private int rows = 24;
  /** The number of columns in the terminal display. */
  private int cols = 80;
  /** The calculated width of a single character in pixels. */
  private int charWidth;
  /** The calculated height of a single character in pixels. */
  private int charHeight;
  /** The calculated descent of the current font in pixels. */
  private int charDescent;

  // Colors for OIA and Cursor
  private static final Color OIA_COLOR = new Color(0, 255, 0); // Green
  private static final Color CURSOR_COLOR_INSERT = new Color(255, 165, 0); // Orange
  private static final Color CURSOR_COLOR_OVERWRITE = new Color(0, 255, 0); // Green

  /**
   * Initializes the terminal panel, setting up the font, input listeners, and 
   * the screen refresh timer.
   */
  public TerminalPanel() {
    setBackground(Color.BLACK);
    setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
    setFocusable(true);
    setFocusTraversalKeysEnabled(false);
    addKeyListener(new TerminalKeyListener());

    updateMetrics();
    Dimension size = new Dimension(cols * charWidth, (rows + 1) * charHeight);
    setPreferredSize(size);
    setMinimumSize(size);

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
        if (session != null) {
          int r = e.getY() / charHeight;
          int c = e.getX() / charWidth;
          if (r >= 0 && r < rows && c >= 0 && c < cols) {
            int addr = r * cols + c;
            session.getScreenBuffer().setCursorAddress(addr);
            session.getScreenBuffer().setCba(addr);
            repaint();
          }
        }
      }
    });

    // Basic refresh timer (10fps)
    new Timer(100, e -> {
      blinkCounter++;
      repaint();
    }).start();
  }

  /**
   * Recalculates font metrics and panel dimensions based on the current font 
   * and terminal model (rows/cols).
   */
  private void updateMetrics() {
    FontMetrics fm = getFontMetrics(getFont());
    if (fm == null) {
      // Fallback if not yet displayable
      charWidth = 12;
      charHeight = 20;
      charDescent = 5;
      return;
    }
    charWidth = fm.charWidth('W');
    charHeight = fm.getHeight();
    charDescent = fm.getDescent();

    ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
    if (session != null && session.getScreenBuffer() != null) {
      int newRows = session.getScreenBuffer().getRows();
      int newCols = session.getScreenBuffer().getCols();
      if (newRows != rows || newCols != cols) {
        rows = newRows;
        cols = newCols;
        Dimension size = new Dimension(cols * charWidth, (rows + 1) * charHeight);
        setPreferredSize(size);
        setMinimumSize(size);
        revalidate();
      }
    }
  }

  /** Counter used for character and cursor blinking animations. */
  private int blinkCounter = 0;

  private class TerminalKeyListener extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
      ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
      if (session == null) return;

      int keyCode = e.getKeyCode();
      if (keyCode == KeyEvent.VK_TAB) {
        if (e.isShiftDown()) {
          session.tabBackward();
        } else {
          session.tabForward();
        }
        return;
      } else if (keyCode == KeyEvent.VK_INSERT) {
        session.toggleInsertMode();
        return;
      } else if (keyCode == KeyEvent.VK_DELETE) {
        session.deleteChar();
        return;
      } else if (keyCode == KeyEvent.VK_END) {
        session.eraseEOF();
        return;
      } else if (keyCode == KeyEvent.VK_HOME || (keyCode == KeyEvent.VK_E && e.isAltDown())) {
        session.eraseInput();
        return;
      } else if (keyCode == KeyEvent.VK_UP) {
        session.cursorUp();
        return;
      } else if (keyCode == KeyEvent.VK_DOWN) {
        session.cursorDown();
        return;
      } else if (keyCode == KeyEvent.VK_LEFT) {
        session.cursorLeft();
        return;
      } else if (keyCode == KeyEvent.VK_RIGHT) {
        session.cursorRight();
        return;
      } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
        session.backspace();
        return;
      } else if (keyCode == KeyEvent.VK_ESCAPE) {
        session.resetKeyboard();
        return;
      }

      AIDKey aidKey = null;
      for (IKeyboardLayout layout : Lookup.getDefault().lookupAll(IKeyboardLayout.class)) {
        aidKey = layout.mapKey(e);
        if (aidKey != null) break;
      }
      if (aidKey != null) {
        session.sendAID(aidKey);
      } else if (isValidChar(e.getKeyChar())) {
        session.sendText(String.valueOf(e.getKeyChar()));
      }
    }

    /**
     * Validates if a character is a printable ASCII character suitable for 
     * terminal entry.
     *
     * @param c The character.
     * @return True if printable.
     */
    private boolean isValidChar(char c) {
      return c >= 32 && c <= 126;
    }
  }

  /**
   * Main rendering logic for the terminal screen and OIA.
   * 
   * <p>Iterates through the {@link ScreenBuffer} to draw characters with their 
   * respective colors and highlights (Reverse Video, Underscore). Also 
   * renders the status bar and the blinking cursor.</p>
   *
   * @param g The graphics context.
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    updateMetrics();

    ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
    if (session == null) {
      return;
    }

    ScreenBuffer buffer = session.getScreenBuffer();
    g.setFont(getFont());

    for (int r = 1; r <= rows; r++) {
      for (int c = 1; c <= cols; c++) {
        ScreenCell cell = buffer.getCell(r, c);
        FieldAttribute attr = cell.getAttribute();

        if (attr.isHidden()) {
          continue;
        }

        Color cellColor = cell.getAttribute().getAwtColor();
        if (cell.getAttribute().getHighlight() == FieldAttribute.HighlightType.REVERSE_VIDEO) {
          g.setColor(cellColor);
          g.fillRect((c - 1) * charWidth, (r - 1) * charHeight, charWidth, charHeight);
          g.setColor(Color.BLACK);
        } else {
          g.setColor(cellColor);
        }

        char content = cell.getContent();
        if (content != '\0') {
          g.drawString(String.valueOf(content), 
                       (c - 1) * charWidth, r * charHeight - charDescent);
        }
        
        if (cell.getAttribute().getHighlight() == FieldAttribute.HighlightType.UNDERSCORE) {
          g.setColor(cellColor);
          g.drawLine((c - 1) * charWidth, r * charHeight - 2, 
                     c * charWidth, r * charHeight - 2);
        }
      }
    }

    // Render Operator Information Area (OIA) on row 25
    g.setColor(OIA_COLOR);
    g.drawLine(0, rows * charHeight, cols * charWidth, rows * charHeight); // Separator line
    
    int cursorR = buffer.getCursorAddress() / cols + 1;
    int cursorC = buffer.getCursorAddress() % cols + 1;
    String cursorText = String.format("%02d/%02d", cursorR, cursorC);
    
    // Status indicators
    if (buffer.isKeyboardLocked()) {
      if (session.isKeyboardError()) {
        g.setColor(Color.RED);
        g.drawString("X - PROT", 5, (rows + 1) * charHeight - charDescent);
        // Beep on the first frame of error
        if (blinkCounter % 10 == 0) {
           Toolkit.getDefaultToolkit().beep();
        }
      } else {
        g.setColor(Color.RED);
        g.drawString("X SYSTEM", 5, (rows + 1) * charHeight - charDescent);
      }
    } else {
      g.setColor(OIA_COLOR);
      g.drawString(session.getStatus(), 5, (rows + 1) * charHeight - charDescent);
    }

    if (session.isInsertMode()) {
      g.setColor(CURSOR_COLOR_INSERT);
      g.drawString("INSERT", 150, (rows + 1) * charHeight - charDescent);
    }
    
    g.setColor(OIA_COLOR);
    g.drawString(cursorText, (cols * charWidth) - 60, (rows + 1) * charHeight - charDescent);
    
    // Render cursor
    if ((blinkCounter / 5) % 2 == 0) {
      int cursorAddr = buffer.getCursorAddress();
      int r = cursorAddr / cols;
      int c = cursorAddr % cols;
      
      if (session.isInsertMode()) {
        g.setColor(CURSOR_COLOR_INSERT);
        g.fillRect(c * charWidth, r * charHeight, charWidth, charHeight); // Solid block
        // Draw character in black over the solid block so it's readable
        g.setColor(Color.BLACK);
        char contentChar = buffer.getChar(r + 1, c + 1);
        if (contentChar != 0) {
          g.drawString(String.valueOf(contentChar), c * charWidth, (r + 1) * charHeight - charDescent);
        }
      } else {
        g.setColor(CURSOR_COLOR_OVERWRITE);
        g.fillRect(c * charWidth, r * charHeight + charHeight - 2, charWidth, 2); // Underline cursor
      }
    }
  }
}
