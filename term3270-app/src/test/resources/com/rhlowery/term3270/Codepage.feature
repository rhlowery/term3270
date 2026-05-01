@issue-40
Feature: EBCDIC Multi-Codepage Support
  As a terminal operator connecting to international
  mainframe systems, I need the emulator to support
  multiple EBCDIC codepages so that characters are
  translated correctly.

  Scenario Outline: Translating characters with
    different codepages
    Given an EBCDIC converter using codepage "<codepage>"
    When I encode the character "<char>"
    And I decode the result
    Then the decoded character should be "<char>"

    Examples:
      | codepage | char |
      | Cp037    | A    |
      | Cp037    | Z    |
      | Cp037    | 0    |
      | Cp037    | 9    |
      | Cp285    | A    |
      | Cp285    | Z    |
      | Cp1140   | A    |
      | Cp1140   | 0    |

  Scenario: Default codepage is Cp037
    Given a default EBCDIC converter
    Then the codepage should be "IBM037"

  Scenario: Invalid codepage is rejected
    When I create a converter with codepage "INVALID"
    Then an UnsupportedCharsetException should be thrown
