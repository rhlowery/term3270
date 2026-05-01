package com.rhlowery.term3270;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for Screen Printing and Clipboard behavior.
 */
public class PrintSteps {

  private final TestContext context;
  private String lastPlainText;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public PrintSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Simulates a request to convert the current screen buffer to plain text.
   */
  @When("I request the screen as plain text")
  public void iRequestTheScreenAsPlainText() {
    lastPlainText = context.getSession().toPlainText();
  }

  /**
   * Verifies that the plain text representation starts with the expected string.
   *
   * @param expected The expected starting text.
   */
  @Then("the text should contain {string} at the beginning")
  public void theTextShouldContainAtTheBeginning(String expected) {
    assertTrue(lastPlainText.startsWith(expected), 
        "Expected text to start with: " + expected);
  }

  /**
   * Verifies that the plain text representation contains specific text at coordinates.
   *
   * @param expected The expected text.
   * @param r        The expected row (1-indexed).
   * @param c        The expected column (1-indexed).
   */
  @Then("the text should contain {string} at position \\({int}, {int})")
  public void theTextShouldContainAtPosition(String expected, Integer r, Integer c) {
    int rows = context.getBuffer().getRows();
    int cols = context.getBuffer().getCols();
    String[] lines = lastPlainText.split("\n");
    assertTrue(lines.length >= r, "Not enough rows in plain text");
    String line = lines[r - 1];
    assertTrue(line.length() >= c + expected.length() - 1, 
        "Line " + r + " too short: " + line.length());
    String actual = line.substring(c - 1, c - 1 + expected.length());
    assertEquals(expected, actual);
  }

  /**
   * Simulates copying the screen buffer to the system clipboard.
   */
  @When("I copy the screen to the clipboard")
  public void iCopyTheScreenToTheClipboard() {
    context.getSession().copyToClipboard();
  }

  /**
   * Verifies that the system clipboard contains the expected terminal text.
   *
   * @throws Exception If clipboard access fails.
   */
  @Then("the system clipboard should contain the terminal text")
  public void theSystemClipboardShouldContainTheTerminalText() throws Exception {
    Transferable contents = Toolkit.getDefaultToolkit()
        .getSystemClipboard().getContents(null);
    String data = (String) contents.getTransferData(DataFlavor.stringFlavor);
    assertEquals(context.getSession().toPlainText(), data);
  }
}
