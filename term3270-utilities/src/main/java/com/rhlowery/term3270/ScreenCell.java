package com.rhlowery.term3270;

import java.awt.Color;

/**
 * Represents a single character position (cell) on the virtual terminal screen.
 * 
 * <p>A cell can either contain a displayable character or serve as a field 
 * attribute definition. It stores the character content, the foreground color, 
 * and a reference to the active {@link FieldAttribute} for its position.</p>
 */
public class ScreenCell {
  /**
   * The displayable character content of the cell.
   */
  private char content = '\0';

  /**
   * The foreground color used for rendering the character.
   */
  private Color foreground = Color.GREEN;

  /**
   * The governing field attribute for this cell's position.
   */
  private FieldAttribute attribute = new FieldAttribute();

  /**
   * Flag indicating if this cell specifically serves as a field attribute definition.
   */
  private boolean isAttributeChar = false;

  /**
   * Constructs a blank screen cell.
   */
  public ScreenCell() {
  }

  /**
   * Returns the displayable character content of the cell.
   *
   * @return The character.
   */
  public char getContent() {
    return content;
  }

  /**
   * Sets the character content of the cell and ensures it is not marked 
   * as an attribute definition.
   *
   * @param content The character.
   */
  public void setContent(char content) {
    this.content = content;
    this.isAttributeChar = false;
  }

  /**
   * Returns the foreground color for rendering the cell.
   *
   * @return The AWT color.
   */
  public Color getForeground() {
    return foreground;
  }

  /**
   * Sets the foreground color for rendering the cell.
   *
   * @param foreground The AWT color.
   */
  public void setForeground(Color foreground) {
    this.foreground = foreground;
  }

  /**
   * Determines if the cell is protected from user input.
   * 
   * <p>A cell is protected if it contains a field attribute definition or if 
   * it belongs to a protected field.</p>
   *
   * @return True if protected, false otherwise.
   */
  public boolean isProtected() {
    return isAttributeChar || attribute.isProtected();
  }

  /**
   * Returns the governing field attribute for this cell's position.
   *
   * @return The field attribute.
   */
  public FieldAttribute getAttribute() {
    return attribute;
  }

  /**
   * Sets the field attribute for this cell, marking it as an attribute definition.
   * 
   * <p>In the 3270 protocol, attribute definitions occupy a space on the screen 
   * but are displayed as blanks.</p>
   *
   * @param attribute The field attribute to apply.
   */
  public void setAttribute(FieldAttribute attribute) {
    this.attribute = attribute;
    this.isAttributeChar = true;
    this.content = ' '; // Attributes are always spaces
  }

  /**
   * Directly sets the field attribute for this cell without marking it 
   * as an attribute definition. Used for internal state synchronization.
   *
   * @param attribute The field attribute.
   */
  public void setFieldAttribute(FieldAttribute attribute) {
    this.attribute = attribute;
  }

  /**
   * Checks if this cell specifically serves as a field attribute definition.
   *
   * @return True if this is an attribute byte position.
   */
  public boolean isAttribute() {
    return isAttributeChar;
  }
}
