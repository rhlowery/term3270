package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openide.util.Lookup;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Common step definitions.
 */
public class CommonSteps {

  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public CommonSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Resets the test environment before each scenario.
   */
  @io.cucumber.java.Before
  public void setUp() {
    ITerminalSession session = context.getSession();
    if (session != null) {
      session.reset();
      // Ensure we reset to default Model 2 size (80x24)
      session.connect(ConnectionConfig.defaultConnection("localhost", 3270, "IBM-3278-2"));
      session.disconnect(); // Go back to DISCONNECTED but with correct buffer size
      session.reset();      // Clear again
      session.getScreenBuffer().setKeyboardLocked(false);
    }
  }

  /**
   * Ensures the screen buffer is cleared and ready for new input.
   */
  @Given("an empty screen")
  public void anEmptyScreen() {
    context.getBuffer().clear();
    context.getBuffer().setKeyboardLocked(false);
    context.getParser().startData();
  }

  /**
   * Resets the terminal buffer to a blank state.
   */
  @Given("a blank terminal buffer")
  public void aBlankTerminalBuffer() {
    context.getBuffer().clear();
    context.getBuffer().setKeyboardLocked(false);
    context.setBuffer(context.getBuffer());
    context.getParser().startData();
  }

  /**
   * Verifies the current status of the terminal session.
   *
   * @param expectedStatus The expected status string.
   */
  @Then("the session status should be {string}")
  public void theSessionStatusShouldBe(String expectedStatus) {
    ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
    assertEquals(expectedStatus, session.getStatus());
  }

  /**
   * Simulates pressing a specific key on the keyboard.
   *
   * @param key The name of the key (e.g., "TAB", "ENTER", "PF1").
   */
  @When("I press the {string} key")
  public void iPressTheKey(String key) {
    ITerminalSession session = Lookup.getDefault().lookup(ITerminalSession.class);
    if ("TAB".equals(key)) {
      session.tabForward();
    } else if ("BACK_TAB".equals(key)) {
      session.tabBackward();
    } else {
      AIDKey aid = AIDKey.valueOf(key);
      session.sendAID(aid);
    }
  }

  /**
   * Simulates parsing a string of text from the host.
   *
   * @param text The text to parse.
   */
  @When("I parse the text {string}")
  public void iParseTheText(String text) {
    byte[] ebcdic = EbcdicConverter.toBytes(text);
    for (byte b : ebcdic) {
      context.getParser().processByte(b);
    }
  }
}
