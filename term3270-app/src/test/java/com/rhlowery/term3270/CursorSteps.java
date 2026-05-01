package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openide.util.Lookup;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for Cursor and Navigation behavior.
 */
public class CursorSteps {
  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public CursorSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Sets the cursor to the specified row and column.
   *
   * @param row The row (1-indexed).
   * @param col The column (1-indexed).
   */
  @Given("the cursor is at row {int} column {int}")
  public void theCursorIsAtRowColumn(Integer row, Integer col) {
    int addr = (row - 1) * 80 + (col - 1);
    context.getSession().getScreenBuffer().setCursorAddress(addr);
    context.getSession().getScreenBuffer().setCba(addr);
  }

  /**
   * Verifies that the cursor is at the expected row and column.
   *
   * @param row The expected row (1-indexed).
   * @param col The expected column (1-indexed).
   */
  @Then("the cursor should be at row {int} column {int}")
  public void theCursorShouldBeAtRowColumn(Integer row, Integer col) {
    int expected = (row - 1) * 80 + (col - 1);
    assertEquals(expected, context.getSession().getScreenBuffer().getCursorAddress());
  }
}
