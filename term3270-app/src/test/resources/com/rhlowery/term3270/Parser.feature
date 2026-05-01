Feature: TN3270 Data Stream Parser
  As a protocol handler
  I want to interpret 3270 orders
  So that the screen is correctly formatted by the mainframe

  Scenario: Parsing Set Buffer Address (SBA)
    Given an empty screen
    When I parse the SBA sequence for row 5 column 10
    Then the current buffer address should be 329

  Scenario: Parsing Start Field (SF)
    Given an empty screen
    When I parse a Start Field order with attribute "PROTECTED"
    Then the cell at the current address should be protected
