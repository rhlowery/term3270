package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for Macro recording and playback behavior.
 */
public class MacroSteps {

  private final TestContext context;
  private List<MacroAction> loadedMacro;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public MacroSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Starts recording a new macro.
   */
  @Given("I start recording a macro")
  public void iStartRecordingAMacro() {
    context.getSession().startRecording();
  }

  /**
   * Stops the current macro recording session.
   */
  @When("I stop recording")
  public void iStopRecording() {
    context.getSession().stopRecording();
  }

  /**
   * Verifies the number of actions in the recorded macro.
   *
   * @param expected The expected number of actions.
   */
  @Then("the recorded macro should have {int} actions")
  public void theRecordedMacroShouldHaveActions(Integer expected) {
    assertEquals(expected, context.getSession().getMacroManager().getCurrentMacro().size());
  }

  /**
   * Plays back the current macro.
   */
  @When("I play back the macro")
  public void iPlayBackTheMacro() {
    context.getSession().getMacroManager().play(
        context.getSession(), 
        context.getSession().getMacroManager().getCurrentMacro());
  }

  /**
   * Simulates pressing an AID key during macro recording.
   *
   * @param keyName The name of the AID key.
   */
  @When("I press the AID key {string}")
  public void iPressTheAIDKey(String keyName) {
    context.getSession().sendAID(AIDKey.valueOf(keyName));
  }

  /**
   * Verifies the last AID key sent during playback.
   *
   * @param expected The expected AID key name.
   */
  @Then("the last sent AID key should be {string}")
  public void theLastSentAIDKeyShouldBe(String expected) {
    // Check the last sent data for the AID code
    byte[] lastData = context.getSession().getLastSentData();
    byte expectedCode = AIDKey.valueOf(expected).getCode();
    assertEquals(expectedCode, lastData[0]);
  }

  /**
   * Defines a macro with a table of actions.
   *
   * @param actions A list of maps representing the macro actions.
   */
  @Given("a macro with actions:")
  public void aMacroWithActions(List<Map<String, String>> actions) {
    context.getSession().startRecording();
    for (Map<String, String> row : actions) {
      MacroAction.ActionType type = MacroAction.ActionType.valueOf(row.get("type"));
      context.getSession().getMacroManager().addAction(
          new MacroAction(type, row.get("value")));
    }
    context.getSession().stopRecording();
  }

  /**
   * Saves the current macro to a file.
   *
   * @param filename The destination filename.
   * @throws Exception If file writing fails.
   */
  @When("I save the macro to {string}")
  public void iSaveTheMacroTo(String filename) throws Exception {
    context.getSession().getMacroManager().saveMacro(new File(filename));
  }

  /**
   * Loads a macro from a file.
   *
   * @param filename The source filename.
   * @throws Exception If file reading fails.
   */
  @When("I load the macro from {string}")
  public void iLoadTheMacroFrom(String filename) throws Exception {
    loadedMacro = context.getSession().getMacroManager().loadMacro(new File(filename));
  }

  /**
   * Verifies the number of actions in a loaded macro.
   *
   * @param expected The expected action count.
   */
  @Then("the loaded macro should have {int} actions")
  public void theLoadedMacroShouldHaveActions(Integer expected) {
    assertEquals(expected, loadedMacro.size());
  }

  /**
   * Verifies the details of a specific action in the loaded macro.
   *
   * @param index    The 1-indexed position of the action.
   * @param expected The expected action text value.
   */
  @Then("action {int} should be TEXT {string}")
  public void actionShouldBeTEXT(Integer index, String expected) {
    MacroAction action = loadedMacro.get(index - 1);
    assertEquals(MacroAction.ActionType.TEXT, action.type());
    assertEquals(expected, action.value());
  }
}
