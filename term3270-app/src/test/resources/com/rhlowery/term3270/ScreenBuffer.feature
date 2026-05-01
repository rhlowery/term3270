Feature: Virtual Screen Buffer
  As a protocol handler
  I want to update the terminal screen data
  So that the user can see the mainframe application output

  Scenario: Updating character data in the buffer
    Given a blank terminal buffer
    When I write "HELLO" at row 1 column 1
    Then the character at row 1 column 1 should be 'H'
    And the character at row 1 column 5 should be 'O'

  Scenario: Clearing the buffer
    Given a buffer with data
    When I clear the screen
    Then all positions should be blank
