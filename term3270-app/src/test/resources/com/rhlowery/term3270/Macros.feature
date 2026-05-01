@issue-45
Feature: Macro Recording and Playback
  As a user
  I want to record my keystrokes as a macro
  So that I can automate repetitive tasks

  Scenario: Recording and playing back text entry
    Given a blank terminal buffer
    And I start recording a macro
    When I type "HELLO"
    And I press the AID key "ENTER"
    And I stop recording
    Then the recorded macro should have 2 actions
    When I play back the macro
    Then the character at row 1 column 1 should be 'H'
    And the character at row 1 column 2 should be 'E'
    And the last sent AID key should be "ENTER"

  Scenario: Saving and loading macros
    Given a macro with actions:
      | type | value |
      | TEXT | USER1 |
      | AID  | ENTER |
    When I save the macro to "test.json"
    And I load the macro from "test.json"
    Then the loaded macro should have 2 actions
    And action 1 should be TEXT "USER1"
