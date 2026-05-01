package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for Cursor Navigation behavior.
 */
public class CursorNavigationSteps {

  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public CursorNavigationSteps(TestContext context) {
    this.context = context;
  }


  /**
   * Simulates pressing the Right arrow key.
   */
  @When("I press the Right arrow key")
  public void iPressTheRightArrowKey() {
    context.getSession().cursorRight();
  }

  /**
   * Simulates pressing the Left arrow key.
   */
  @When("I press the Left arrow key")
  public void iPressTheLeftArrowKey() {
    context.getSession().cursorLeft();
  }

  /**
   * Simulates pressing the Up arrow key.
   */
  @When("I press the Up arrow key")
  public void iPressTheUpArrowKey() {
    context.getSession().cursorUp();
  }

  /**
   * Simulates pressing the Down arrow key.
   */
  @When("I press the Down arrow key")
  public void iPressTheDownArrowKey() {
    context.getSession().cursorDown();
  }

  /**
   * Simulates pressing the Backspace key.
   */
  @When("I press the Backspace key")
  public void iPressTheBackspaceKey() {
    context.getSession().backspace();
  }

}
