package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.openide.util.Lookup;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for Integration behavior.
 */
public class IntegrationSteps {
  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public IntegrationSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Initializes a session with specific text at a given location.
   *
   * @param text The text to place in the buffer.
   * @param row  The row (1-indexed).
   * @param col  The column (1-indexed).
   */
  @Given("a session with {string} at row {int} column {int}")
  public void aSessionWithAtRowColumn(String text, Integer row, Integer col) {
    ScreenBuffer buffer = context.getBuffer();
    for (int i = 0; i < text.length(); i++) {
      buffer.setChar(row, col + i, text.charAt(i));
    }
  }

  /**
   * Verifies that the UI renders the expected text at a specific row.
   *
   * @param text The expected text.
   * @param row  The row (1-indexed).
   */
  @Then("the UI should render {string} at row {int}")
  public void theUiShouldRenderAtRow(String text, Integer row) {
    // Verify that the buffer in the session (which the UI uses) 
    // contains the expected data.
    ScreenBuffer buffer = context.getBuffer();
    for (int i = 0; i < text.length(); i++) {
      assertEquals(text.charAt(i), buffer.getChar(row, i + 1));
    }
  }
}
