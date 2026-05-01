Feature: Keyboard Input
  As a user
  I want to interact with the terminal using my keyboard
  So that I can send commands and data to the mainframe

  Scenario Outline: Mapping physical keys to AID actions
    Given a focused terminal window
    When I physically press the "<physical_key>" key
    Then the session should trigger a "<aid_action>" action

    Examples:
      | physical_key | aid_action |
      | ENTER        | ENTER      |
      | F1           | PF1        |
      | F12          | PF12       |
      | ESCAPE       | CLEAR      |
