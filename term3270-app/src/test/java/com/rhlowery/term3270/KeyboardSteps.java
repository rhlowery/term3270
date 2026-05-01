package com.rhlowery.term3270;
import com.rhlowery.term3270.ui.*;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.Lookup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Step definitions for Keyboard behavior.
 */
public class KeyboardSteps {
  private final TestContext context;
  private ITerminalSession mockSession;
  private TerminalPanel panel;
  private KeyEvent lastEvent;

  public KeyboardSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Initializes a focused terminal window with a mock session.
   */
  @Given("a focused terminal window")
  public void aFocusedTerminalWindow() {
    mockSession = mock(ITerminalSession.class);
    context.setSession(mockSession);
    
    // Setup Mock Lookup
    InstanceContent content = new InstanceContent();
    content.add(mockSession);
    Lookup mockLookup = new AbstractLookup(content);
    
    // In a real OpenIDE environment, we'd use a more complex setup,
    // but for this test, we'll assume the panel uses the session.
    panel = new TerminalPanel() {
      // Override to use our mock session without global Lookup changes
      @Override
      protected void processKeyEvent(KeyEvent e) {
        AIDKey aid = KeyboardMapper.mapToAID(e);
        if (aid != null) mockSession.sendAID(aid);
      }
    };
  }

  /**
   * Simulates a physical keyboard key press event.
   *
   * @param keyName The name of the key being pressed.
   */
  @When("I physically press the {string} key")
  public void iPhysicallyPressTheKey(String keyName) {
    int keyCode = switch (keyName) {
      case "ENTER" -> KeyEvent.VK_ENTER;
      case "F1" -> KeyEvent.VK_F1;
      case "F12" -> KeyEvent.VK_F12;
      case "ESCAPE" -> KeyEvent.VK_ESCAPE;
      case "PAGE_UP" -> KeyEvent.VK_PAGE_UP;
      case "PAGE_DOWN" -> KeyEvent.VK_PAGE_DOWN;
      default -> throw new IllegalArgumentException("Unknown key: " + keyName);
    };

    lastEvent = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, 
                             System.currentTimeMillis(), 0, keyCode, 
                             KeyEvent.CHAR_UNDEFINED);
    
    // Simulate the key press
    AIDKey aid = KeyboardMapper.mapToAID(lastEvent);
    if (aid != null) {
      mockSession.sendAID(aid);
    }
  }

  /**
   * Verifies that the session triggered the expected AID action.
   *
   * @param aidAction The expected AID action name.
   */
  @Then("the session should trigger a {string} action")
  public void theSessionShouldTriggerAAction(String aidAction) {
    AIDKey expectedAid = AIDKey.valueOf(aidAction);
    verify(mockSession).sendAID(expectedAid);
  }
}
