package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for Field Validation behavior.
 */
public class FieldValidationSteps {

  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public FieldValidationSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Creates a numeric field at the specified coordinates.
   *
   * @param row The row (1-indexed).
   * @param col The column (1-indexed).
   */
  @Given("a numeric field at row {int} column {int}")
  public void aNumericFieldAtRowColumn(Integer row, Integer col) {
    FieldAttribute attr = new FieldAttribute();
    attr.setProtected(false);
    attr.setNumeric(true);
    context.getBuffer().setAttribute(row, col, attr);
  }

  /**
   * Simulates typing text at a specific location on the screen.
   *
   * @param text The text to type.
   * @param row  The starting row (1-indexed).
   * @param col  The starting column (1-indexed).
   */
  @When("I type {string} at row {int} column {int}")
  public void iTypeAtRowColumn(String text, Integer row, Integer col) {
    int startAddr = (row - 1) * 80 + (col - 1);
    context.getBuffer().setCba(startAddr);
    // Typing simulation (respects numeric validation)
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      context.getBuffer().writeAtCba(c);
    }
  }
}
