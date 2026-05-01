Feature: 3270 Field Attributes
  As a user
  I want the terminal to respect field attributes
  So that I only enter data where allowed and see highlighted text

  Scenario: Protected fields prevent data entry
    Given a blank terminal buffer
    And a protected field at row 5 column 10
    When I write "TEST" at row 5 column 10
    Then the character at row 5 column 10 should be ' '

  Scenario: Intensified display
    Given a cell with "INTENSIFIED" attribute at row 1 column 1
    Then the cell at row 1 column 1 should be intensified

  Scenario: Color attribute
    Given a blank terminal buffer
    When the host parses a color "BLUE" for cell at row 1 column 1
    Then the cell at row 1 column 1 should be "BLUE"
