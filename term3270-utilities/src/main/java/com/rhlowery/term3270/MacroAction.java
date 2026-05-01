package com.rhlowery.term3270;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable record representing a single recorded or playable action in a macro.
 * 
 * <p>Macros consist of a sequence of actions, either entering text or pressing 
 * an AID key. These actions are serialized to JSON for persistence.</p>
 * 
 * @param type  The type of action (TEXT or AID).
 * @param value The text to enter or the name of the AID key to press.
 */
public record MacroAction(
    @JsonProperty("type") ActionType type,
    @JsonProperty("value") String value
) {
  /**
   * Defines the categories of actions that can be recorded in a macro.
   */
  public enum ActionType {
    /** Represents entering a string of characters into the buffer. */
    TEXT,
    /** Represents pressing a terminal function or attention key. */
    AID
  }
}
