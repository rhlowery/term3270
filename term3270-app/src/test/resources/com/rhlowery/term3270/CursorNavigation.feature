Feature: Cursor Arrow Navigation and Backspace
  As a user
  I want to be able to move the cursor freely using arrow keys and backspace
  So that I can edit specific parts of the screen easily

  Background:
    Given a blank terminal buffer

  Scenario: Move cursor right
    Given the cursor is at row 1 column 1
    When I press the Right arrow key
    Then the cursor should be at row 1 column 2

  Scenario: Move cursor right wraps to next line
    Given the cursor is at row 1 column 80
    When I press the Right arrow key
    Then the cursor should be at row 2 column 1

  Scenario: Move cursor left wraps to previous line
    Given the cursor is at row 2 column 1
    When I press the Left arrow key
    Then the cursor should be at row 1 column 80

  Scenario: Move cursor left wraps from top to bottom
    Given the cursor is at row 1 column 1
    When I press the Left arrow key
    Then the cursor should be at row 24 column 80

  Scenario: Move cursor up wraps to bottom
    Given the cursor is at row 1 column 5
    When I press the Up arrow key
    Then the cursor should be at row 24 column 5

  Scenario: Move cursor down wraps to top
    Given the cursor is at row 24 column 10
    When I press the Down arrow key
    Then the cursor should be at row 1 column 10

  Scenario: Backspace moves left and deletes
    Given the screen has an unprotected field from row 1 column 1 to row 1 column 10
    And I write "TEST" at row 1 column 1
    And the cursor is at row 1 column 5
    When I press the Backspace key
    Then the cursor should be at row 1 column 4
    And the character at row 1 column 1 should be 'T'
    And the character at row 1 column 2 should be 'E'
    And the character at row 1 column 3 should be 'S'
    And the character at row 1 column 4 should be ' '
