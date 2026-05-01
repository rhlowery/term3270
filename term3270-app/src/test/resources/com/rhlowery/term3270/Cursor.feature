Feature: Cursor Management and Tab Navigation
  As a user
  I want the cursor to behave like a standard 3270 terminal
  So that I can navigate input fields efficiently

  Scenario: Tabbing to the next unprotected field
    Given an empty screen
    And an unprotected field at row 1 column 9
    And an unprotected field at row 2 column 9
    And the cursor is at row 1 column 1
    When I press the "TAB" key
    Then the cursor should be at row 1 column 10
    When I press the "TAB" key
    Then the cursor should be at row 2 column 10

  Scenario: Backtabbing to the previous unprotected field
    Given an empty screen
    And an unprotected field at row 1 column 9
    And an unprotected field at row 2 column 9
    And the cursor is at row 2 column 15
    When I press the "BACK_TAB" key
    Then the cursor should be at row 2 column 10
    When I press the "BACK_TAB" key
    Then the cursor should be at row 1 column 10
