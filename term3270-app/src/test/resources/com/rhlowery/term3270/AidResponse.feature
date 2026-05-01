Feature: 3270 AID Response Processing

  Scenario: Sending Modified Data on ENTER
    Given a blank terminal buffer
    And an unprotected field at row 1 column 1
    And I write "FOO" at row 1 column 2
    When I press the "ENTER" key
    Then the outbound data should contain AID "ENTER"
    And the outbound data should contain cursor address at row 1 column 5
    And the outbound data should contain "FOO" for field at row 1 column 1

  Scenario: Resetting MDT on WCC
    Given a blank terminal buffer
    And an unprotected field at row 1 column 1
    And I write "BAR" at row 1 column 2
    Then the field at row 1 column 1 should be modified
    When I parse a WCC with reset MDT bit set
    Then the field at row 1 column 1 should not be modified
