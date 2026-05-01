@issue-51
Feature: IBM 5250 Protocol Support
  As a user connecting to an IBM i (AS/400) system,
  I want the terminal to correctly parse 5250 data streams
  so that I can interact with the host.

  Background:
    Given a screen buffer with 24 rows and 80 columns
    And a 5250 data stream parser

  Scenario: Parse basic 5250 Write to Display order
    When the host sends 5250 data "040000110101"
    And the host sends 5250 character "A"
    Then the character at row 1 column 1 should be "A"

  Scenario: Parse 5250 Start of Field order
    Given the 5250 parser is in data mode
    When the host sends 5250 data "1D40"
    Then the cell at row 1 column 1 should be an attribute
