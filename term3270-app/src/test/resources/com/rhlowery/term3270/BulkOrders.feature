Feature: 3270 Bulk Orders (RA and EUA)
  As a protocol handler
  I want to interpret bulk screen update orders
  So that the screen is efficiently updated by the mainframe

  Scenario: Repeating a character to an address (RA)
    Given an empty screen
    When I parse an RA sequence for row 1 column 10 with char '*'
    Then the character at row 1 column 1 should be '*'
    And the character at row 1 column 9 should be '*'
    And the current buffer address should be 9

  Scenario: Erasing unprotected fields to an address (EUA)
    Given a blank terminal buffer
    And an unprotected field at row 1 column 1
    And I write "HELLO" at row 1 column 1
    And a protected field at row 1 column 10
    When I parse the SBA sequence for row 1 column 1
    And I parse an EUA sequence for row 1 column 10
    Then the character at row 1 column 1 should be ' '
    And the character at row 1 column 10 should be ' '
