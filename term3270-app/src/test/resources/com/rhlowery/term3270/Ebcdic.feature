Feature: EBCDIC-to-ASCII Character Mapping
  As a protocol handler
  I want to translate mainframe EBCDIC bytes to ASCII characters
  So that the user can read the application text

  Scenario: Translating EBCDIC alphanumeric characters
    When I translate EBCDIC byte 193
    Then the ASCII character should be "A"
    When I translate EBCDIC byte 241
    Then the ASCII character should be "1"

  Scenario: Handling special characters
    When I translate EBCDIC byte 75
    Then the ASCII character should be "."
