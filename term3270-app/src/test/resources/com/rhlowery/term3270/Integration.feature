Feature: UI and Buffer Integration
  As a user
  I want to see the contents of the session buffer on my screen
  So that I can read mainframe data

  Scenario: Rendering buffer content to the screen
    Given a blank terminal buffer
    When I write "WELCOME" at row 1 column 1
    Then the character at row 1 column 1 should be "W"
    And the character at row 1 column 7 should be "E"
