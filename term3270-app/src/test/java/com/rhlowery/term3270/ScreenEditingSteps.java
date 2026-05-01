package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

/**
 * Step definitions for Screen Editing behavior.
 */
public class ScreenEditingSteps {

  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public ScreenEditingSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Defines an unprotected field range on the screen.
   *
   * @param startR Start row (1-indexed).
   * @param startC Start column (1-indexed).
   * @param endR   End row (1-indexed).
   * @param endC   End column (1-indexed).
   */
  @Given("the screen has an unprotected field from row {int} column {int} to row {int} column {int}")
  public void theScreenHasAnUnprotectedField(Integer startR, Integer startC, Integer endR, Integer endC) {
    ScreenBuffer buffer = context.getBuffer();
    // In 3270, an unprotected field starts after an attribute byte.
    // If the field is at (1,1), we might need an attribute at (24,80) or similar.
    // To keep it simple, we put an unprotected attribute at the cell BEFORE the field starts
    int beforeStartC = startC - 1;
    int beforeStartR = startR;
    if (beforeStartC < 1) {
      beforeStartC = 80;
      beforeStartR--;
      if (beforeStartR < 1) beforeStartR = 24;
    }
    
    FieldAttribute unprotectedAttr = new FieldAttribute();
    unprotectedAttr.setProtected(false);
    buffer.setAttribute(beforeStartR, beforeStartC, unprotectedAttr);

    FieldAttribute protectedAttr = new FieldAttribute();
    protectedAttr.setProtected(true);
    buffer.setAttribute(endR, endC + 1, protectedAttr);
  }

  /**
   * Defines a protected field range on the screen.
   *
   * @param startR Start row (1-indexed).
   * @param startC Start column (1-indexed).
   * @param endR   End row (1-indexed).
   * @param endC   End column (1-indexed).
   */
  @Given("the screen has a protected field from row {int} column {int} to row {int} column {int}")
  public void theScreenHasAProtectedField(Integer startR, Integer startC, Integer endR, Integer endC) {
    ScreenBuffer buffer = context.getBuffer();
    int beforeStartC = startC - 1;
    int beforeStartR = startR;
    if (beforeStartC < 1) {
      beforeStartC = 80;
      beforeStartR--;
      if (beforeStartR < 1) beforeStartR = 24;
    }
    
    FieldAttribute protectedAttr = new FieldAttribute();
    protectedAttr.setProtected(true);
    buffer.setAttribute(beforeStartR, beforeStartC, protectedAttr);
  }

  /**
   * Moves the cursor to the specified row and column.
   *
   * @param row Target row (1-indexed).
   * @param col Target column (1-indexed).
   */
  @When("I move the cursor to row {int} column {int}")
  public void iMoveTheCursorToRowColumn(Integer row, Integer col) {
    int index = (row - 1) * 80 + (col - 1);
    context.getBuffer().setCba(index);
    context.getBuffer().setCursorAddress(index);
  }

  /**
   * Simulates pressing the Delete key.
   */
  @When("I press the Delete key")
  public void iPressTheDeleteKey() {
    context.getSession().deleteChar();
  }

  /**
   * Simulates pressing the Insert key to toggle insert mode.
   */
  @When("I press the Insert key to toggle insert mode")
  public void iPressTheInsertKeyToToggleInsertMode() {
    context.getSession().toggleInsertMode();
  }

  /**
   * Simulates typing a string of text.
   *
   * @param text The text to type.
   */
  @When("I type {string}")
  public void iType(String text) {
    context.getSession().sendText(text);
  }

  /**
   * Simulates pressing the Erase EOF key.
   */
  @When("I press Erase EOF")
  public void iPressEraseEOF() {
    context.getSession().eraseEOF();
  }

  /**
   * Simulates pressing the Erase Input key.
   */
  @When("I press Erase Input")
  public void iPressEraseInput() {
    context.getSession().eraseInput();
  }
}
