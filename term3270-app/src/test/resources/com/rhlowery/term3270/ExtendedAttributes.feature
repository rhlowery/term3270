Feature: 3270 Extended Attributes
  As a user
  I want the terminal to display colors and highlighting
  So that I can clearly distinguish different types of data

  Scenario: Setting field color via SFE
    Given a blank terminal buffer
    When I parse an SFE sequence with color "BLUE" at row 1 column 10
    And I parse the text "COLOR"
    Then the character at row 1 column 11 should have color "BLUE"

  Scenario: Setting field highlight via SFE
    Given a blank terminal buffer
    When I parse an SFE sequence with highlight "UNDERSCORE" at row 1 column 10
    And I parse the text "HL"
    Then the character at row 1 column 11 should have highlight "UNDERSCORE"

  Scenario: Applying highlighting via SA
    Given a blank terminal buffer
    When I parse the SBA sequence for row 1 column 1
    And I parse an SA sequence for "UNDERSCORE"
    And I parse the text "TEST"
    Then the character at row 1 column 1 should have highlight "UNDERSCORE"
    And the character at row 1 column 4 should have highlight "UNDERSCORE"

  Scenario: Applying color via SA
    Given a blank terminal buffer
    When I parse the SBA sequence for row 1 column 10
    And I parse an SA sequence for color "BLUE"
    And I parse the text "SA"
    Then the character at row 1 column 10 should have color "BLUE"
