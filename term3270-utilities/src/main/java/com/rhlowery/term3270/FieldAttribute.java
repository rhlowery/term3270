package com.rhlowery.term3270;

import java.awt.Color;

/**
 * Represents a 3270 field attribute or extended attribute set.
 * 
 * <p>In the 3270 protocol, fields are defined by an attribute byte that specifies 
 * protection (protected/unprotected), alphanumeric type (numeric/alpha), 
 * display intensity (normal/high/hidden), and the Modified Data Tag (MDT).</p>
 * 
 * <p>This class also supports extended attributes such as colors and 
 * highlighting (blink, reverse video, underscore) used in more modern 3270 
 * implementations.</p>
 */
public class FieldAttribute {

  /**
   * Defines extended colors supported by TN3270 extended attributes.
   */
  public enum ExtendedColor {
    /** Default color based on protection and intensity. */
    NEUTRAL(0x00),
    /** Standard IBM Blue. */
    BLUE(0xF1),
    /** Standard IBM Red. */
    RED(0xF2),
    /** Standard IBM Pink. */
    PINK(0xF3),
    /** Standard IBM Green. */
    GREEN(0xF4),
    /** Standard IBM Turquoise. */
    TURQUOISE(0xF5),
    /** Standard IBM Yellow. */
    YELLOW(0xF6),
    /** Standard IBM White. */
    WHITE(0xF7);

    /**
     * The internal protocol code for the color.
     */
    private final int code;

    /**
     * Constructs an ExtendedColor with its protocol code.
     *
     * @param code The protocol code.
     */
    ExtendedColor(int code) {
      this.code = code;
    }

    /**
     * Translates a protocol byte code into an ExtendedColor enum member.
     *
     * @param code The attribute byte code.
     * @return The corresponding ExtendedColor.
     */
    public static ExtendedColor fromCode(int code) {
      for (ExtendedColor c : values()) {
        if (c.code == code) return c;
      }
      return NEUTRAL;
    }
  }

  /**
   * Defines highlighting types for the screen display.
   */
  public enum HighlightType {
    /** Standard display. */
    NORMAL(0x00),
    /** Blinking text. */
    BLINK(0xF1),
    /** Inverted background/foreground. */
    REVERSE_VIDEO(0xF2),
    /** Underlined text. */
    UNDERSCORE(0xF4);

    /**
     * The internal protocol code for the highlight type.
     */
    private final int code;

    /**
     * Constructs a HighlightType with its protocol code.
     *
     * @param code The protocol code.
     */
    HighlightType(int code) {
      this.code = code;
    }

    /**
     * Translates a protocol byte code into a HighlightType enum member.
     *
     * @param code The attribute byte code.
     * @return The corresponding HighlightType.
     */
    public static HighlightType fromCode(int code) {
      for (HighlightType h : values()) {
        if (h.code == code) return h;
      }
      return NORMAL;
    }
  }

  /**
   * Flag indicating if the field is protected.
   */
  private boolean isProtected = false;

  /**
   * Flag indicating if the field is numeric-only.
   */
  private boolean isNumeric = false;

  /**
   * Flag indicating if the field is hidden (non-display).
   */
  private boolean isHidden = false;

  /**
   * Flag indicating if the field is displayed with high intensity.
   */
  private boolean isIntensified = false;

  /**
   * Flag representing the Modified Data Tag (MDT).
   */
  private boolean isModified = false;

  /**
   * The extended color assigned to this field.
   */
  private ExtendedColor color = ExtendedColor.NEUTRAL;

  /**
   * The highlighting type assigned to this field.
   */
  private HighlightType highlight = HighlightType.NORMAL;

  /**
   * Constructs a default attribute instance (Unprotected, Alpha, Normal intensity).
   */
  public FieldAttribute() {
    this.isProtected = false;
    this.isNumeric = false;
  }

  /**
   * Constructs an attribute instance from a standard 3270 attribute byte.
   *
   * @param b The attribute byte received from the host.
   */
  public FieldAttribute(byte b) {
    isProtected = (b & 0x20) != 0;
    isNumeric = (b & 0x10) != 0;
    isIntensified = (b & 0x08) != 0; // Simplified: bit 3 and 4 together
    isHidden = (b & 0x0C) == 0x0C;   // Both bits 2 and 3 set
    isModified = (b & 0x01) != 0;
  }

  /**
   * Checks if the field is protected from user input.
   *
   * @return True if protected.
   */
  public boolean isProtected() {
    return isProtected;
  }

  /**
   * Sets whether the field is protected.
   *
   * @param protected1 True for protected.
   */
  public void setProtected(boolean protected1) {
    isProtected = protected1;
  }

  /**
   * Checks if the field is hidden (non-display).
   *
   * @return True if hidden.
   */
  public boolean isHidden() {
    return isHidden;
  }

  /**
   * Checks if the field only accepts numeric input.
   *
   * @return True if numeric.
   */
  public boolean isNumeric() {
    return isNumeric;
  }

  /**
   * Sets whether the field is numeric-only.
   *
   * @param numeric True for numeric-only.
   */
  public void setNumeric(boolean numeric) {
    isNumeric = numeric;
  }

  /**
   * Checks if the field is displayed with high intensity.
   *
   * @return True if intensified.
   */
  public boolean isIntensified() {
    return isIntensified;
  }

  /**
   * Sets the display intensity of the field.
   *
   * @param intensified True for high intensity.
   */
  public void setIntensified(boolean intensified) {
    isIntensified = intensified;
  }

  /**
   * Checks the Modified Data Tag (MDT) status of the field.
   *
   * @return True if modified.
   */
  public boolean isModified() {
    return isModified;
  }

  /**
   * Sets the Modified Data Tag (MDT) status.
   *
   * @param modified True if modified.
   */
  public void setModified(boolean modified) {
    isModified = modified;
  }

  /**
   * Sets whether the field is hidden.
   *
   * @param hidden True for hidden.
   */
  public void setHidden(boolean hidden) {
    isHidden = hidden;
  }

  /**
   * Returns the extended color assigned to this field.
   *
   * @return The extended color.
   */
  public ExtendedColor getColor() {
    return color;
  }

  /**
   * Sets the extended color for this field.
   *
   * @param color The extended color.
   */
  public void setColor(ExtendedColor color) {
    this.color = color;
  }

  /**
   * Returns the highlighting type assigned to this field.
   *
   * @return The highlighting type.
   */
  public HighlightType getHighlight() {
    return highlight;
  }

  /**
   * Sets the highlighting type for this field.
   *
   * @param highlight The highlighting type.
   */
  public void setHighlight(HighlightType highlight) {
    this.highlight = highlight;
  }
  
  /**
   * Converts the attribute flags back to a standard 3270 attribute byte.
   *
   * @return The 3270 attribute byte.
   */
  public byte toByte() {
    int b = 0x00;
    if (isProtected) b |= 0x20;
    if (isNumeric) b |= 0x10;
    if (isIntensified) b |= 0x08;
    if (isHidden) b |= 0x0C; // Both bits set for hidden
    if (isModified) b |= 0x01;
    
    // Map to valid 3270 byte (6-bit range)
    // This is a bit simplified, usually 3270 bytes are 0x40 - 0x7F
    return (byte) b;
  }
  
  /**
   * Translates the internal attribute state into an AWT color for rendering.
   * 
   * <p>Standard 3270 colors (Green, Red, Blue, White) are chosen based on 
   * protection and intensity if no extended color is set.</p>
   *
   * @return The AWT color to use for rendering characters in this field.
   */
  public Color getAwtColor() {
    return switch (color) {
      case BLUE -> new Color(100, 150, 255); // A more readable blue on black background
      case RED -> Color.RED;
      case PINK -> Color.MAGENTA;
      case GREEN -> Color.GREEN;
      case TURQUOISE -> Color.CYAN;
      case YELLOW -> Color.YELLOW;
      case WHITE -> Color.WHITE;
      case NEUTRAL -> {
        if (isProtected) {
          yield isIntensified ? Color.WHITE : new Color(100, 150, 255); // Standard: Protected Normal = Blue
        } else {
          yield isIntensified ? Color.RED : Color.GREEN; // Standard: Unprotected High = Red, Normal = Green
        }
      }
    };
  }
}
