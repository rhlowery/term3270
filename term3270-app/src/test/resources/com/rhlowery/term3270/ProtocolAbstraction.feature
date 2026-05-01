@issue-41
Feature: Protocol Abstraction for 5250 Compatibility
  As a developer extending the emulator, I need a
  common parser interface so that new protocols can
  be plugged in without modifying session logic.

  Scenario: TN3270 parser implements the common interface
    Given a TN3270 data stream parser
    Then it should implement the common parser interface

  Scenario: Parser can be swapped at the session level
    Given a default terminal session
    Then the parser should implement the common interface
