Feature: 3270 Field Validation and Formatting

  Scenario: Rejecting non-numeric input in numeric fields
    Given a blank terminal buffer
    And a numeric field at row 1 column 1
    When I type "A" at row 1 column 2
    Then the character at row 1 column 2 should be " "
    
  Scenario: Accepting numeric input in numeric fields
    Given a blank terminal buffer
    And a numeric field at row 1 column 1
    When I type "1" at row 1 column 2
    Then the character at row 1 column 2 should be "1"

  Scenario: Auto-skipping when field is full
    Given a blank terminal buffer
    And an unprotected field at row 1 column 1
    And an unprotected field at row 1 column 5
    When I write "123" at row 1 column 2
    Then the cursor should be at row 1 column 6
