package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for Attribute behavior.
 */
public class AttributeSteps {

  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public AttributeSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Creates a protected field at the specified coordinates.
   *
   * @param row The row (1-indexed).
   * @param col The column (1-indexed).
   */
  @Given("a protected field at row {int} column {int}")
  public void aProtectedFieldAtRowColumn(Integer row, Integer col) {
    FieldAttribute attr = new FieldAttribute();
    attr.setProtected(true);
    context.getBuffer().setAttribute(row, col, attr);
  }

  /**
   * Creates a field with a specific attribute at the specified coordinates.
   *
   * @param attrName The name of the attribute (e.g., "INTENSIFIED").
   * @param row      The row (1-indexed).
   * @param col      The column (1-indexed).
   */
  @Given("a cell with {string} attribute at row {int} column {int}")
  public void aCellWithAttributeAtRowColumn(String attrName, Integer row, Integer col) {
    FieldAttribute attr = new FieldAttribute();
    if ("INTENSIFIED".equals(attrName)) {
      attr.setIntensified(true);
    }
    context.getBuffer().setAttribute(row, col, attr);
  }

  /**
   * Verifies that the cell at the specified coordinates is intensified.
   *
   * @param row The row (1-indexed).
   * @param col The column (1-indexed).
   */
  @Then("the cell at row {int} column {int} should be intensified")
  public void theCellAtRowColumnShouldBeIntensified(Integer row, Integer col) {
    assertTrue(context.getBuffer().getCell(row, col).getAttribute().isIntensified());
  }

  /**
   * Simulates the host parsing a color attribute for a specific cell.
   *
   * @param colorName The name of the color.
   * @param row       The row (1-indexed).
   * @param col       The column (1-indexed).
   */
  @When("the host parses a color {string} for cell at row {int} column {int}")
  public void theHostParsesAColorForCellAtRowColumn(String colorName, Integer row, Integer col) {
    IDataStreamParser parser = context.getParser();
    parser.reset();
    parser.startData();
    
    // Send SFE: 29 <count> <type> <val>
    byte[] sfe = new byte[5];
    sfe[0] = 0x29; // SFE
    sfe[1] = 0x01; // 1 attribute
    sfe[2] = 0x42; // Color type
    sfe[3] = (byte) FieldAttribute.ExtendedColor.valueOf(colorName).ordinal(); // This is wrong, need the 0xFx code
    
    // Actually use fromCode logic
    FieldAttribute.ExtendedColor color = FieldAttribute.ExtendedColor.valueOf(colorName);
    // Hardcode some for now or use reflection if I had it
    byte colorCode = switch(color) {
        case BLUE -> (byte)0xF1;
        case RED -> (byte)0xF2;
        case GREEN -> (byte)0xF4;
        default -> 0;
    };
    sfe[3] = colorCode;
    
    // Set position first
    parser.processByte((byte)0x11); // SBA
    byte[] addr = AddressConverter.fromAddress((row - 1) * 80 + (col - 1));
    parser.processByte(addr[0]);
    parser.processByte(addr[1]);
    
    for (byte b : sfe) {
        if (b != 0) parser.processByte(b);
    }
  }

  /**
   * Verifies that the cell at the specified coordinates has the expected color.
   *
   * @param row           The row (1-indexed).
   * @param col           The column (1-indexed).
   * @param expectedColor The expected color name.
   */
  @Then("the cell at row {int} column {int} should be {string}")
  public void theCellAtRowColumnShouldBe(Integer row, Integer col, String expectedColor) {
    assertEquals(expectedColor, context.getBuffer().getCell(row, col).getAttribute().getColor().name());
  }
}
