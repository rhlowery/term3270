package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for ScreenBuffer behavior.
 */
public class ScreenBufferSteps {

  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public ScreenBufferSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Writes text to the buffer at the specified coordinates.
   *
   * @param text The text to write.
   * @param row  The starting row (1-indexed).
   * @param col  The starting column (1-indexed).
   */
  @When("I write {string} at row {int} column {int}")
  public void iWriteAtRowColumn(String text, Integer row, Integer col) {
    int startAddr = (row - 1) * 80 + (col - 1);
    ScreenBuffer buffer = context.getBuffer();
    buffer.setCba(startAddr);
    for (int i = 0; i < text.length(); i++) {
      buffer.writeAtCba(text.charAt(i));
    }
  }

  /**
   * Verifies the character at the specified coordinates.
   *
   * @param row      The row (1-indexed).
   * @param col      The column (1-indexed).
   * @param expected The expected character string.
   */
  @Then("the character at row {int} column {int} should be {string}")
  public void theCharacterAtRowColumnShouldBe(Integer row, Integer col, String expected) {
    assertEquals(expected.charAt(0), context.getBuffer().getChar(row, col));
  }

  /**
   * Initializes the buffer with some default data for testing.
   */
  @Given("a buffer with data")
  public void aBufferWithData() {
    context.getBuffer().clear();
    context.getBuffer().setChar(1, 1, 'X');
  }

  /**
   * Clears the entire screen buffer.
   */
  @When("I clear the screen")
  public void iClearTheScreen() {
    context.getBuffer().clear();
  }

  /**
   * Verifies that all positions in the buffer are blank.
   */
  @Then("all positions should be blank")
  public void allPositionsShouldBeBlank() {
    for (int r = 1; r <= context.getBuffer().getRows(); r++) {
      for (int c = 1; c <= context.getBuffer().getCols(); c++) {
        assertEquals(' ', context.getBuffer().getChar(r, c));
      }
    }
  }

  /**
   * Simulates the host parsing text starting at specific coordinates.
   *
   * @param text The text for the host to parse.
   * @param row  The starting row (1-indexed).
   * @param col  The starting column (1-indexed).
   */
  @When("the host parses the text {string} starting at row {int} column {int}")
  public void theHostParsesTheTextStartingAtRowColumn(String text, Integer row, Integer col) {
    IDataStreamParser parser = context.getParser();
    parser.reset();
    parser.startData();
    
    // Set address via SBA
    parser.processByte((byte) 0x11); // SBA
    byte[] addr = AddressConverter.fromAddress((row - 1) * context.getBuffer().getCols() + (col - 1));
    parser.processByte(addr[0]);
    parser.processByte(addr[1]);
    
    // Send text
    byte[] ebcdic = EbcdicConverter.toBytes(text);
    for (byte b : ebcdic) {
      parser.processByte(b);
    }
  }

  /**
   * Verifies the current buffer address (CBA) coordinates.
   *
   * @param row Expected row (1-indexed).
   * @param col Expected column (1-indexed).
   */
  @Then("the current buffer address should be at row {int} column {int}")
  public void theCurrentBufferAddressShouldBeAtRowColumn(Integer row, Integer col) {
    int expected = (row - 1) * context.getBuffer().getCols() + (col - 1);
    assertEquals(expected, context.getBuffer().getCba());
  }

  /**
   * Verifies the cursor position coordinates.
   *
   * @param row Expected row (1-indexed).
   * @param col Expected column (1-indexed).
   */
  @Then("the cursor should remain at row {int} column {int}")
  public void theCursorShouldRemainAtRowColumn(Integer row, Integer col) {
    int expected = (row - 1) * context.getBuffer().getCols() + (col - 1);
    assertEquals(expected, context.getBuffer().getCursorAddress());
  }
}
