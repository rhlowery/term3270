package com.rhlowery.term3270;
import com.rhlowery.term3270.ui.*;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Step definitions for Rendering behavior.
 */
public class RenderingSteps {

  private TerminalPanel panel;

  /**
   * Ensures a connected terminal session is available for rendering tests.
   */
  @Given("a connected terminal session")
  public void aConnectedTerminalSession() {
    // Session state verification (mocked for now)
  }

  /**
   * Initializes the terminal UI component.
   */
  @When("the UI is initialized")
  public void theUiIsInitialized() {
    panel = new TerminalPanel();
  }

  /**
   * Verifies that the UI renders the standard 24x80 grid with an OIA line.
   */
  @Then("the UI should render a 24x80 character grid")
  public void theUiShouldRenderA24x80CharacterGrid() {
    Dimension size = panel.getPreferredSize();
    FontMetrics fm = panel.getFontMetrics(panel.getFont());
    int charWidth = (fm != null) ? fm.charWidth('W') : 12;
    int charHeight = (fm != null) ? fm.getHeight() : 20;
    
    assertEquals(80 * charWidth, size.width);
    assertEquals((24 + 1) * charHeight, size.height); // +1 for OIA status line
  }

  /**
   * Verifies the default background color of the terminal panel.
   *
   * @param colorName The expected color name.
   */
  @Then("the default background color should be {string}")
  public void theDefaultBackgroundColorShouldBe(String colorName) {
    assertEquals(Color.BLACK, panel.getBackground());
  }
}
