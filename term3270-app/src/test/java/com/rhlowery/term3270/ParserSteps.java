package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for Parser behavior.
 */
public class ParserSteps {

  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public ParserSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Simulates parsing a Set Buffer Address (SBA) sequence.
   *
   * @param row The target row (1-indexed).
   * @param col The target column (1-indexed).
   */
  @When("I parse the SBA sequence for row {int} column {int}")
  public void iParseTheSbaSequenceForRowColumn(Integer row, Integer col) {
    int addr = (row - 1) * 80 + (col - 1);
    byte[] codes = AddressConverter.fromAddress(addr);
    
    context.getParser().processByte((byte) 0x11); // SBA Order
    context.getParser().processByte(codes[0]);
    context.getParser().processByte(codes[1]);
  }

  /**
   * Simulates the host parsing an SBA sequence.
   *
   * @param row The target row (1-indexed).
   * @param col The target column (1-indexed).
   */
  @When("the host parses a SBA sequence for row {int} column {int}")
  public void theHostParsesASbaSequenceForRowColumn(Integer row, Integer col) {
    iParseTheSbaSequenceForRowColumn(row, col);
  }

  /**
   * Verifies the current buffer address (CBA).
   *
   * @param expectedAddr The expected 0-indexed address.
   */
  @Then("the current buffer address should be {int}")
  public void theCurrentBufferAddressShouldBe(Integer expectedAddr) {
    assertEquals(expectedAddr, context.getBuffer().getCba());
  }

  /**
   * Simulates parsing a Start Field (SF) order with a specific attribute.
   *
   * @param attrType The attribute type (e.g., "PROTECTED").
   */
  @When("I parse a Start Field order with attribute {string}")
  public void iParseAStartFieldOrderWithAttribute(String attrType) {
    byte attrByte = 0x00;
    if ("PROTECTED".equals(attrType)) {
      attrByte = 0x20;
    }
    
    context.getParser().processByte((byte) 0x1D); // SF Order
    context.getParser().processByte(attrByte);
  }

  /**
   * Verifies that the cell at the current address is protected.
   */
  @Then("the cell at the current address should be protected")
  public void theCellAtTheCurrentAddressShouldBeProtected() {
    // SF sets attribute at current address and increments CBA, 
    // so we check CBA - 1
    int addr = context.getBuffer().getCba() - 1;
    assertTrue(context.getBuffer()
               .getCell(addr / 80 + 1, addr % 80 + 1)
               .isProtected());
  }

  /**
   * Simulates parsing a Program Tab (PT) order.
   */
  @When("the host parses a PT sequence")
  public void iParseAPtSequence() {
    context.getParser().processByte((byte) 0x05); // PT Order
  }
}
