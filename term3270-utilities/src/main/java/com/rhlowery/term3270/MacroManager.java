package com.rhlowery.term3270;

import org.openide.util.Lookup;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the capture, persistence, and playback of terminal macros.
 * 
 * <p>A macro consists of a sequence of {@link MacroAction} objects. This manager 
 * provides methods to start/stop recording, add actions to the current macro, 
 * serialize the macro to JSON using Jackson, and execute a sequence of actions 
 * against an active {@link ITerminalSession}.</p>
 */
public class MacroManager {

  /**
   * The internal list of actions in the current macro sequence.
   */
  private final List<MacroAction> currentMacro = new ArrayList<>();

  /**
   * Flag indicating if a macro recording is currently active.
   */
  private boolean recording = false;

  /**
   * Starts recording user actions into the current macro.
   */
  public void startRecording() {
    currentMacro.clear();
    recording = true;
  }

  /**
   * Stops the current macro recording session.
   */
  public void stopRecording() {
    recording = false;
  }

  /**
   * Returns whether a macro recording is currently in progress.
   *
   * @return True if recording.
   */
  public boolean isRecording() {
    return recording;
  }

  /**
   * Adds an action to the current macro sequence if recording is active.
   *
   * @param action The action to add (TEXT or AID).
   */
  public void addAction(MacroAction action) {
    if (recording) {
      currentMacro.add(action);
    }
  }

  /**
   * Returns a copy of the currently recorded macro actions.
   *
   * @return A list of macro actions.
   */
  public List<MacroAction> getCurrentMacro() {
    return new ArrayList<>(currentMacro);
  }

  /**
   * Serializes the current macro to a file using an available store.
   *
   * @param file The target file.
   * @throws Exception If serialization fails or no store is found.
   */
  public void saveMacro(File file) throws Exception {
    for (IMacroStore store : Lookup.getDefault().lookupAll(IMacroStore.class)) {
      if (store.supports("JSON")) {
        store.save(currentMacro, file);
        return;
      }
    }
    throw new Exception("No JSON macro store found.");
  }

  /**
   * Loads a macro sequence from a file using an available store.
   *
   * @param file The source file.
   * @return The list of macro actions loaded.
   * @throws Exception If deserialization fails or no store is found.
   */
  public List<MacroAction> loadMacro(File file) throws Exception {
    for (IMacroStore store : Lookup.getDefault().lookupAll(IMacroStore.class)) {
      if (store.supports("JSON")) {
        List<MacroAction> loaded = store.load(file);
        currentMacro.clear();
        currentMacro.addAll(loaded);
        return loaded;
      }
    }
    throw new Exception("No JSON macro store found.");
  }

  /**
   * Executes a sequence of actions against the provided terminal session.
   *
   * @param session The session to play the macro on.
   * @param actions The list of actions to execute.
   */
  public void play(ITerminalSession session, List<MacroAction> actions) {
    for (MacroAction action : actions) {
      if (action.type() == MacroAction.ActionType.TEXT) {
        session.sendText(action.value());
      } else if (action.type() == MacroAction.ActionType.AID) {
        AIDKey key = AIDKey.valueOf(action.value());
        session.sendAID(key);
      }
      // Simple delay to simulate realistic typing and give host time to react
      try { Thread.sleep(100); } catch (InterruptedException e) { }
    }
  }
}
