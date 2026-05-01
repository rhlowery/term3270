package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for Function Key and Keyboard Reset behavior.
 */
public class FunctionKeySteps {

  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public FunctionKeySteps(TestContext context) {
    this.context = context;
  }

  /**
   * Ensures the keyboard is in a locked state.
   */
  @Given("the keyboard is locked")
  public void theKeyboardIsLocked() {
    context.getBuffer().setKeyboardLocked(true);
  }

  /**
   * Simulates clicking a specific UI button (e.g., "Reset").
   *
   * @param buttonName The name of the button.
   */
  @When("I click the {string} button")
  public void iClickTheButton(String buttonName) {
    if ("Reset".equals(buttonName)) {
      context.getSession().resetKeyboard();
    }
  }

  /**
   * Verifies that the keyboard has been unlocked.
   */
  @Then("the keyboard should be unlocked")
  public void theKeyboardShouldBeUnlocked() {
    assertFalse(context.getBuffer().isKeyboardLocked());
  }
}
