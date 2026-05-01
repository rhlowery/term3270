package com.rhlowery.term3270;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for Extended Attribute behavior.
 */
public class ExtendedAttributeSteps {

  private final TestContext context;
  private IDataStreamParser parser;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public ExtendedAttributeSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Simulates parsing a Start Field Extended (SFE) sequence with a color attribute.
   *
   * @param colorName The name of the color.
   * @param row       The row (1-indexed).
   * @param col       The column (1-indexed).
   */
  @When("I parse an SFE sequence with color {string} at row {int} column {int}")
  public void iParseAnSfeSequenceWithColorAtRowColumn(String colorName, Integer row, Integer col) {
    int addr = (row - 1) * 80 + (col - 1);
    byte[] codes = AddressConverter.fromAddress(addr);
    
    // Position the CBA
    context.getParser().processByte((byte) 0x11); // SBA
    context.getParser().processByte(codes[0]);
    context.getParser().processByte(codes[1]);

    // SFE sequence
    context.getParser().processByte((byte) 0x29); // SFE
    context.getParser().processByte((byte) 0x01); // 1 attribute pair
    context.getParser().processByte((byte) 0x42); // Color type
    
    FieldAttribute.ExtendedColor color = FieldAttribute.ExtendedColor.valueOf(colorName);
    byte colorCode = switch (color) {
      case BLUE -> (byte) 0xF1;
      case RED -> (byte) 0xF2;
      case PINK -> (byte) 0xF3;
      case GREEN -> (byte) 0xF4;
      case TURQUOISE -> (byte) 0xF5;
      case YELLOW -> (byte) 0xF6;
      case WHITE -> (byte) 0xF7;
      default -> (byte) 0x00;
    };
    context.getParser().processByte(colorCode);
  }

  /**
   * Simulates parsing a Start Field Extended (SFE) sequence with a highlighting attribute.
   *
   * @param hlName The name of the highlight type.
   * @param row    The row (1-indexed).
   * @param col    The column (1-indexed).
   */
  @When("I parse an SFE sequence with highlight {string} at row {int} column {int}")
  public void iParseAnSfeSequenceWithHighlightAtRowColumn(String hlName, Integer row, Integer col) {
    int addr = (row - 1) * 80 + (col - 1);
    byte[] codes = AddressConverter.fromAddress(addr);
    
    context.getParser().processByte((byte) 0x11); // SBA
    context.getParser().processByte(codes[0]);
    context.getParser().processByte(codes[1]);

    context.getParser().processByte((byte) 0x29); // SFE
    context.getParser().processByte((byte) 0x01); // 1 pair
    context.getParser().processByte((byte) 0x41); // Highlight type
    
    FieldAttribute.HighlightType hl = FieldAttribute.HighlightType.valueOf(hlName);
    byte hlCode = switch (hl) {
      case BLINK -> (byte) 0xF1;
      case REVERSE_VIDEO -> (byte) 0xF2;
      case UNDERSCORE -> (byte) 0xF4;
      default -> (byte) 0x00;
    };
    context.getParser().processByte(hlCode);
  }

  /**
   * Simulates parsing a Set Attribute (SA) sequence for a highlight type.
   *
   * @param highlightName The name of the highlight type.
   */
  @When("I parse an SA sequence for {string}")
  public void iParseAnSaSequenceFor(String highlightName) {
    context.getParser().processByte((byte) 0x28); // SA
    context.getParser().processByte((byte) 0x41); // Highlight type
    
    FieldAttribute.HighlightType hl = FieldAttribute.HighlightType.valueOf(highlightName);
    byte hlCode = switch (hl) {
      case BLINK -> (byte) 0xF1;
      case REVERSE_VIDEO -> (byte) 0xF2;
      case UNDERSCORE -> (byte) 0xF4;
      default -> (byte) 0x00;
    };
    context.getParser().processByte(hlCode);
  }

  /**
   * Simulates parsing a Set Attribute (SA) sequence for a color.
   *
   * @param colorName The name of the color.
   */
  @When("I parse an SA sequence for color {string}")
  public void iParseAnSaSequenceForColor(String colorName) {
    context.getParser().processByte((byte) 0x28); // SA
    context.getParser().processByte((byte) 0x42); // Color type
    
    FieldAttribute.ExtendedColor color = FieldAttribute.ExtendedColor.valueOf(colorName);
    byte colorCode = switch (color) {
      case BLUE -> (byte) 0xF1;
      case RED -> (byte) 0xF2;
      case PINK -> (byte) 0xF3;
      case GREEN -> (byte) 0xF4;
      case TURQUOISE -> (byte) 0xF5;
      case YELLOW -> (byte) 0xF6;
      case WHITE -> (byte) 0xF7;
      default -> (byte) 0x00;
    };
    context.getParser().processByte(colorCode);
  }

  /**
   * Verifies the color of the character at the specified coordinates.
   *
   * @param row       The row (1-indexed).
   * @param col       The column (1-indexed).
   * @param colorName The expected color name.
   */
  @Then("the character at row {int} column {int} should have color {string}")
  public void theCharacterAtRowColumnShouldHaveColor(Integer row, Integer col, String colorName) {
    ScreenCell cell = context.getBuffer().getCell(row, col);
    FieldAttribute.ExtendedColor expected = FieldAttribute.ExtendedColor.valueOf(colorName);
    assertEquals(expected, cell.getAttribute().getColor());
  }

  /**
   * Verifies the highlight of the character at the specified coordinates.
   *
   * @param row    The row (1-indexed).
   * @param col    The column (1-indexed).
   * @param hlName The expected highlight type name.
   */
  @Then("the character at row {int} column {int} should have highlight {string}")
  public void theCharacterAtRowColumnShouldHaveHighlight(Integer row, Integer col, String hlName) {
    ScreenCell cell = context.getBuffer().getCell(row, col);
    FieldAttribute.HighlightType expected = FieldAttribute.HighlightType.valueOf(hlName);
    assertEquals(expected, cell.getAttribute().getHighlight());
  }
}
