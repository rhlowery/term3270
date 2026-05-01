package com.rhlowery.term3270;

import java.io.ByteArrayOutputStream;

/**
 * Represents the virtual terminal screen buffer for a 3270 session.
 * 
 * <p>This class manages a grid of {@link ScreenCell} objects, maintaining the 
 * character content and attributes for each position. It provides methods for 
 * both host-driven updates (data stream parsing) and user-driven interaction 
 * (typing, shifting, deleting).</p>
 * 
 * <p>The buffer also tracks the Current Buffer Address (CBA), the cursor 
 * position, and the keyboard lock/error states.</p>
 */
public class ScreenBuffer {
  /**
   * The number of rows in the terminal screen.
   */
  private int rows;

  /**
   * The number of columns in the terminal screen.
   */
  private int cols;

  /**
   * The flat array of cells representing the screen grid.
   */
  private ScreenCell[] cells;

  /**
   * The Current Buffer Address (CBA) for host-driven writes.
   */
  private int cba = 0;

  /**
   * The current position of the cursor (0-indexed).
   */
  private int cursorAddress = 0;

  /**
   * Flag indicating if the keyboard is locked.
   */
  private boolean keyboardLocked = true;

  /**
   * Flag indicating if the keyboard is in a logical error state.
   */
  private boolean keyboardError = false;

  /**
   * The EBCDIC converter used for character encoding translation.
   */
  private EbcdicConverter converter = EbcdicConverter.defaultConverter();

  // Remove duplicate/empty Javadoc block
  /**
   * Translates the entire screen buffer into a plain text representation.
   * 
   * <p>Null characters and attributes are represented as spaces. Each row is 
   * separated by a newline character.</p>
   *
   * @return The plain text screen contents.
   */
  public String toPlainText() {
    StringBuilder sb = new StringBuilder();
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        char content = cells[r * cols + c].getContent();
        sb.append(content == '\0' ? ' ' : content);
      }
      if (r < rows - 1) {
        sb.append('\n');
      }
    }
    return sb.toString();
  }

  /**
   * Sets the EBCDIC converter for character encoding translation.
   *
   * @param converter The converter instance to use.
   */
  public void setConverter(EbcdicConverter converter) {
    this.converter = converter;
  }

  /**
   * Constructs an empty screen buffer with default 24x80 dimensions.
   */
  public ScreenBuffer() {
    this(24, 80);
  }

  /**
   * Constructs an empty screen buffer with specified dimensions.
   *
   * @param rows The number of rows.
   * @param cols The number of columns.
   */
  public ScreenBuffer(int rows, int cols) {
    this.rows = rows;
    this.cols = cols;
    this.cells = new ScreenCell[rows * cols];
    clear();
  }

  /** @return The number of rows in the buffer. */
  public int getRows() { return rows; }
  /** @return The number of columns in the buffer. */
  public int getCols() { return cols; }

  /**
   * Resizes the screen buffer to new dimensions.
   *
   * @param rows The new number of rows.
   * @param cols The new number of columns.
   */
  public void resize(int rows, int cols) {
    if (this.rows == rows && this.cols == cols) return;
    this.rows = rows;
    this.cols = cols;
    this.cells = new ScreenCell[rows * cols];
    clear();
  }

  private boolean formatted = false;

  /**
   * Clears the screen buffer.
   */
  public final void clear() {
    for (int i = 0; i < cells.length; i++) {
      cells[i] = new ScreenCell();
    }
    cba = 0;
    cursorAddress = 0;
    keyboardLocked = true;
    formatted = false;
  }

  /**
   * Sets a character at a specific coordinate.
   *
   * @param row 1-indexed row.
   * @param col 1-indexed column.
   * @param c The character.
   */
  public void setChar(int row, int col, char c) {
    int index = getIndex(row, col);
    ScreenCell cell = cells[index];
    cell.setContent(c);
    FieldAttribute attr = findFieldAttribute(index);
    if (attr != null) {
      attr.setModified(true);
    }
  }

  /**
   * Sets the attribute for a specific cell.
   *
   * @param row 1-indexed row.
   * @param col 1-indexed column.
   * @param attr The attribute.
   */
  public void setAttribute(int row, int col, FieldAttribute attr) {
    int index = getIndex(row, col);
    cells[index].setAttribute(attr);
    formatted = true;
  }

  /**
   * Gets a character at a specific coordinate.
   *
   * @param row 1-indexed row.
   * @param col 1-indexed column.
   * @return The character.
   */
  public char getChar(int row, int col) {
    char c = cells[getIndex(row, col)].getContent();
    return c == '\0' ? ' ' : c;
  }

  /**
   * Gets the full cell state at a specific coordinate.
   *
   * @param row 1-indexed row.
   * @param col 1-indexed column.
   * @return The ScreenCell.
   */
  public ScreenCell getCell(int row, int col) {
    return cells[getIndex(row, col)];
  }

  /**
   * Returns the current buffer address (CBA) used for host-driven writes.
   *
   * @return The 0-indexed buffer address.
   */
  public int getCba() {
    return cba;
  }

  /**
   * Sets the current buffer address (CBA).
   *
   * @param cba The 0-indexed buffer address.
   */
  public void setCba(int cba) {
    this.cba = cba % (rows * cols);
  }

  /**
   * Returns the current cursor address on the screen.
   *
   * @return The 0-indexed cursor position.
   */
  public int getCursorAddress() {
    return cursorAddress;
  }

  /**
   * Sets the current cursor address.
   *
   * @param cursorAddress The 0-indexed cursor position.
   */
  public void setCursorAddress(int cursorAddress) {
    this.cursorAddress = cursorAddress % (rows * cols);
  }

  /**
   * Writes a character at the current buffer address (CBA) with full 3270 
   * field validation.
   * 
   * <p>This method handles protected field checks, numeric-only validation, 
   * and automatic cursor advancement to the next unprotected position.</p>
   *
   * @param c The character to write.
   */
  public void writeAtCba(char c) {
    ScreenCell cell = cells[cba];
    if (cell.isProtected() || cell.isAttribute()) {
      int nextUnprotected = findNextUnprotected(cba);
      if (nextUnprotected == cba) { // No unprotected fields found
        keyboardError = true;
        return;
      }
      cba = nextUnprotected;
      cursorAddress = cba;
      cell = cells[cba]; // Refresh cell reference
    }

    FieldAttribute attr = findFieldAttribute(cba);
    if (attr != null && attr.isNumeric() && !Character.isDigit(c)) {
      keyboardError = true;
      return;
    }

    cell.setContent(c);
    if (attr != null) {
      attr.setModified(true);
    }

    int next = (cba + 1) % cells.length;
    if (cells[next].isAttribute()) {
      if (cells[next].getAttribute().isProtected()) {
        cba = findNextUnprotected(next);
      } else {
        cba = (next + 1) % cells.length; // Skip the unprotected attribute byte
      }
    } else {
      cba = next;
    }
    cursorAddress = cba;
  }

  /**
   * Writes a character at the current buffer address without any protection checks
   * or field skipping logic. Used for Host data stream writes.
   *
   * @param c The character to write.
   */
  public void writeRawAtCba(char c) {
    cells[cba].setContent(c);
    cba = (cba + 1) % cells.length;
  }

  /**
   * Finds the end address of the current field (the position before the next attribute).
   *
   * @param start The starting address.
   * @return The 0-indexed address of the last character in the field.
   */
  public int findEndOfCurrentField(int start) {
    int size = rows * cols;
    int current = start;
    for (int i = 0; i < size; i++) {
      if (cells[current].isAttribute()) {
        return (current - 1 + size) % size;
      }
      current = (current + 1) % size;
    }
    return (start - 1 + size) % size;
  }

  /**
   * Inserts a character at the current CBA by shifting existing content.
   * 
   * <p>If the field is protected or the cursor is on an attribute, the 
   * cursor is first moved to the next unprotected field. If the field is 
   * full, the keyboard is logically locked.</p>
   *
   * @param c The character to insert.
   */
  public void insertAtCursor(char c) {
    ScreenCell cell = cells[cba];
    if (cell.isProtected() || cell.isAttribute()) {
      int nextUnprotected = findNextUnprotected(cba);
      if (nextUnprotected == cba) { // No unprotected fields found
        keyboardError = true;
        return;
      }
      cba = nextUnprotected;
      cursorAddress = cba;
      cell = cells[cba]; // Refresh cell reference
    }

    FieldAttribute attr = findFieldAttribute(cba);
    if (attr != null && attr.isNumeric() && !Character.isDigit(c) && c != ' ') {
      keyboardError = true;
      return; // Keyboard lock logically
    }

    int endIndex = findEndOfCurrentField(cba);
    
    // Check if field is full
    if (cells[endIndex].getContent() != ' ' && cells[endIndex].getContent() != '\0') {
      return; // Keyboard lock logically
    }

    int size = rows * cols;
    // Shift right
    int current = endIndex;
    while (current != cba) {
      int prev = (current - 1 + size) % size;
      cells[current].setContent(cells[prev].getContent());
      current = prev;
    }

    cells[cba].setContent(c);
    if (attr != null) {
      attr.setModified(true);
    }

    // Advance cursor
    int next = (cba + 1) % size;
    if (cells[next].isAttribute()) {
      if (cells[next].getAttribute().isProtected()) {
        cba = findNextUnprotected(next);
      } else {
        cba = (next + 1) % size; // Skip the unprotected attribute byte
      }
    } else {
      cba = next;
    }
    cursorAddress = cba;
  }

  /**
   * Deletes the character at CBA, shifting the rest of the unprotected field to the left.
   */
  public void deleteAtCursor() {
    ScreenCell cell = cells[cba];
    if (cell.isProtected() || cell.isAttribute()) {
      return;
    }

    int endIndex = findEndOfCurrentField(cba);
    int size = rows * cols;

    // Shift left
    int current = cba;
    while (current != endIndex) {
      int next = (current + 1) % size;
      cells[current].setContent(cells[next].getContent());
      current = next;
    }
    cells[endIndex].setContent(' ');

    FieldAttribute attr = findFieldAttribute(cba);
    if (attr != null) {
      attr.setModified(true);
    }
  }

  /**
   * Erases all characters from CBA to the end of the current unprotected field.
   */
  public void eraseEndOfField() {
    ScreenCell cell = cells[cba];
    if (cell.isProtected() || cell.isAttribute()) {
      return;
    }

    int endIndex = findEndOfCurrentField(cba);
    int size = rows * cols;

    int current = cba;
    while (true) {
      cells[current].setContent(' ');
      if (current == endIndex) break;
      current = (current + 1) % size;
    }

    FieldAttribute attr = findFieldAttribute(cba);
    if (attr != null) {
      attr.setModified(true);
    }
  }

  /**
   * Erases all unprotected fields on the screen and resets MDTs.
   * Moves cursor to the first unprotected field.
   */
  public void eraseInput() {
    int firstUnprotected = -1;
    for (int i = 0; i < cells.length; i++) {
      if (cells[i].isAttribute()) {
        cells[i].getAttribute().setModified(false);
        if (firstUnprotected == -1 && !cells[i].getAttribute().isProtected()) {
          firstUnprotected = (i + 1) % cells.length;
        }
      } else if (!cells[i].isProtected()) {
        cells[i].setContent(' ');
      }
    }

    if (firstUnprotected != -1) {
      cba = firstUnprotected;
      cursorAddress = cba;
    }
  }

  /**
   * Repeats a character from current CBA to stop address.
   *
   * @param stopAddress The address to stop at.
   * @param c The character.
   */
  public void repeatToAddress(int stopAddress, char c) {
    int current = cba;
    while (current != stopAddress) {
      cells[current].setContent(c);
      current = (current + 1) % (rows * cols);
    }
    cba = stopAddress;
  }

  /**
   * Erases unprotected cells from current CBA to stop address.
   *
   * @param stopAddress The address to stop at.
   */
  public void eraseUnprotectedToAddress(int stopAddress) {
    int current = cba;
    while (current != stopAddress) {
      if (!cells[current].isProtected()) {
        cells[current].setContent(' ');
      }
      current = (current + 1) % (rows * cols);
    }
    cba = stopAddress;
  }

  /**
   * Searches for the next available unprotected field in the buffer.
   *
   * @param start The address to begin the search from.
   * @return The address of the first character position in the next unprotected field.
   */
  public int findNextUnprotected(int start) {
    int size = rows * cols;
    int current = (start + 1) % size;
    while (current != start) {
      if (cells[current].isAttribute() && !cells[current].getAttribute().isProtected()) {
        return (current + 1) % size;
      }
      current = (current + 1) % size;
    }
    return start;
  }

  /**
   * Finds the start of the previous unprotected field.
   *
   * @param start The current address to start searching from.
   * @return The address of the first character of the previous unprotected field.
   */
  public int findPreviousUnprotected(int start) {
    int size = rows * cols;
    int current = (start - 1 + size) % size;
    // Step 1: Find the start of the current field if we are in one
    // Actually, 3270 Backtab goes to the start of the current field, 
    // or if already there, the start of the previous one.
    
    // Scan backwards for an attribute
    while (current != start) {
      if (cells[current].isAttribute()) {
        if (!cells[current].getAttribute().isProtected()) {
          // Found start of a field. 
          // If this is the current field's start, keep going.
          int fieldStart = (current + 1) % size;
          if (fieldStart != start) {
            return fieldStart;
          }
        }
      }
      current = (current - 1 + size) % size;
    }
    return start;
  }

  /**
   * Scans backwards from the specified index to find the start-field attribute 
   * governing that position.
   *
   * @param index The buffer index.
   * @return The governing FieldAttribute, or null if none found.
   */
  public FieldAttribute findFieldAttribute(int index) {
    int size = rows * cols;
    int current = index;
    for (int i = 0; i < size; i++) {
      if (cells[current].isAttribute()) {
        return cells[current].getAttribute();
      }
      current = (current - 1 + size) % size;
    }
    return null;
  }


  private int getIndex(int row, int col) {
    if (row < 1 || row > rows || col < 1 || col > cols) {
      throw new IllegalArgumentException("Coordinates out of bounds");
    }
    return (row - 1) * cols + (col - 1);
  }

  /**
   * Resets the Modified Data Tag (MDT) for all fields.
   */
  public void resetModified() {
    for (ScreenCell cell : cells) {
      if (cell.isAttribute()) {
        cell.getAttribute().setModified(false);
      }
    }
  }

  /**
   * Scans the buffer for fields with the Modified Data Tag (MDT) set and 
   * generates a 3270 data stream for transmission to the host.
   *
   * @return A byte array containing the SBA and data for all modified fields.
   */
  public byte[] readModified() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    if (!formatted) {
      // Unformatted buffer: Read everything up to the last non-null character
      int lastNonNull = -1;
      for (int i = cells.length - 1; i >= 0; i--) {
        if (cells[i].getContent() != '\0') {
          lastNonNull = i;
          break;
        }
      }
      
      for (int i = 0; i <= lastNonNull; i++) {
        char c = cells[i].getContent();
        if (c != '\0') {
          out.write(converter.encode(c));
        }
      }
      return out.toByteArray();
    }
    
    int size = cells.length;
    for (int i = 0; i < size; i++) {
      if (cells[i].isAttribute() && cells[i].getAttribute().isModified()) {
        out.write(0x11); // SBA
        byte[] addr = AddressConverter.fromAddress((i + 1) % size);
        out.write(addr[0]);
        out.write(addr[1]);

        int j = (i + 1) % size;
        while (!cells[j].isAttribute() && j != i) {
          char c = cells[j].getContent();
          if (c != '\0') {
            out.write(converter.encode(c));
          }
          j = (j + 1) % size;
        }
      }
    }
    return out.toByteArray();
  }

  /**
   * Generates a full 3270 data stream representing the entire screen buffer.
   *
   * @return A byte array containing all fields and attributes in the buffer.
   */
  public byte[] readBuffer() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (int i = 0; i < cells.length; i++) {
      if (cells[i].isAttribute()) {
        out.write(0x1D); // SF
        out.write(cells[i].getAttribute().toByte());
      } else {
        out.write(converter.encode(cells[i].getContent()));
      }
    }
    return out.toByteArray();
  }

  /**
   * Returns whether the keyboard is locked (either by the host or due to an error).
   *
   * @return True if locked.
   */
  public boolean isKeyboardLocked() {
    return keyboardLocked || keyboardError;
  }

  /**
   * Returns whether the keyboard is in a logical error state.
   *
   * @return True if in an error state.
   */
  public boolean isKeyboardError() {
    return keyboardError;
  }

  /**
   * Sets the keyboard lock status.
   * 
   * <p>If unlocked, any existing keyboard error state is also cleared.</p>
   *
   * @param locked True to lock the keyboard, false to unlock.
   */
  public void setKeyboardLocked(boolean locked) {
    this.keyboardLocked = locked;
    if (!locked) {
      this.keyboardError = false;
    }
  }

  /**
   * Resets the Modified Data Tag (MDT) for all fields in the buffer.
   */
  public void resetMdt() {
    for (ScreenCell cell : cells) {
      if (cell.isAttribute()) {
        cell.getAttribute().setModified(false);
      }
    }
  }
}
