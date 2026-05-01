Feature: Host Data Stream Rendering

  Scenario: Host writes into a protected field
    Given a blank terminal buffer
    And a protected field at row 1 column 1
    When the host parses the text "HOSTDATA" starting at row 1 column 1
    Then the character at row 1 column 1 should be 'H'
    And the character at row 1 column 2 should be 'O'
    And the current buffer address should be at row 1 column 9

  Scenario: Host writes across field boundaries
    Given a blank terminal buffer
    And I move the cursor to row 1 column 1
    And the screen has an unprotected field from row 1 column 1 to row 1 column 5
    And the screen has a protected field from row 1 column 6 to row 1 column 10
    When the host parses the text "OVERFLOW" starting at row 1 column 1
    Then the character at row 1 column 1 should be 'O'
    And the character at row 1 column 4 should be 'R'
    And the character at row 1 column 6 should be 'L'
    And the character at row 1 column 8 should be 'W'
    And the current buffer address should be at row 1 column 9
    And the cursor should remain at row 1 column 1

  Scenario: Host uses Program Tab (PT)
    Given a blank terminal buffer
    And an unprotected field at row 5 column 1
    When the host parses a SBA sequence for row 1 column 1
    And the host parses a PT sequence
    Then the current buffer address should be at row 5 column 2
