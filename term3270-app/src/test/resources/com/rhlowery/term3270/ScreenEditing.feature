Feature: Screen Editing Operations
  As a user
  I want to be able to edit text in unprotected fields using standard keys
  So that I can correct mistakes before sending data to the host

  Background:
    Given a blank terminal buffer
    And the screen has an unprotected field from row 1 column 1 to row 1 column 10

  Scenario: Delete character at cursor
    When I write "HELLO" at row 1 column 1
    And I move the cursor to row 1 column 2
    And I press the Delete key
    Then the character at row 1 column 1 should be 'H'
    And the character at row 1 column 2 should be 'L'
    And the character at row 1 column 3 should be 'L'
    And the character at row 1 column 4 should be 'O'

  Scenario: Insert character at cursor
    When I press the Insert key to toggle insert mode
    And I write "HELO" at row 1 column 1
    And I move the cursor to row 1 column 3
    And I type 'L'
    Then the character at row 1 column 1 should be 'H'
    And the character at row 1 column 2 should be 'E'
    And the character at row 1 column 3 should be 'L'
    And the character at row 1 column 4 should be 'L'
    And the character at row 1 column 5 should be 'O'

  Scenario: Erase End of Field
    When I write "HELLO WORLD" at row 1 column 1
    And I move the cursor to row 1 column 6
    And I press Erase EOF
    Then the character at row 1 column 5 should be 'O'
    And the character at row 1 column 6 should be ' '
    And the character at row 1 column 7 should be ' '

  Scenario: Erase Input
    When I write "TEST" at row 1 column 1
    And I press Erase Input
    Then the character at row 1 column 1 should be ' '
    And the character at row 1 column 2 should be ' '
