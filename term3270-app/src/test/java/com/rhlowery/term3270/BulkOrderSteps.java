package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for Bulk Order behavior.
 */
public class BulkOrderSteps {

  private final TestContext context;
  private IDataStreamParser parser;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public BulkOrderSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Simulates parsing a Repeat to Address (RA) sequence.
   *
   * @param row The target row (1-indexed).
   * @param col The target column (1-indexed).
   * @param c   The character to repeat.
   */
  @When("I parse an RA sequence for row {int} column {int} with char {string}")
  public void iParseAnRaSequenceForRowColumnWithChar(Integer row, Integer col, String c) {
    int addr = (row - 1) * 80 + (col - 1);
    byte[] codes = AddressConverter.fromAddress(addr);
    
    context.getParser().processByte((byte) 0x3C); // RA Order
    context.getParser().processByte(codes[0]);
    context.getParser().processByte(codes[1]);
    context.getParser().processByte(EbcdicConverter.toEbcdic(c.charAt(0)));
  }

  /**
   * Creates an unprotected field at the specified coordinates.
   *
   * @param row The row (1-indexed).
   * @param col The column (1-indexed).
   */
  @Given("an unprotected field at row {int} column {int}")
  public void anUnprotectedFieldAtRowColumn(Integer row, Integer col) {
    FieldAttribute attr = new FieldAttribute();
    attr.setProtected(false);
    context.getBuffer().setAttribute(row, col, attr);
  }

  /**
   * Simulates parsing an Erase Unprotected to Address (EUA) sequence.
   *
   * @param row The target row (1-indexed).
   * @param col The target column (1-indexed).
   */
  @When("I parse an EUA sequence for row {int} column {int}")
  public void iParseAnEuaSequenceForRowColumn(Integer row, Integer col) {
    int addr = (row - 1) * 80 + (col - 1);
    byte[] codes = AddressConverter.fromAddress(addr);
    
    context.getParser().processByte((byte) 0x12); // EUA Order
    context.getParser().processByte(codes[0]);
    context.getParser().processByte(codes[1]);
  }
}
