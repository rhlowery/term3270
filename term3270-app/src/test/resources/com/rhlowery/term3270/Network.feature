Feature: TN3270 Telnet Connection
  As a user
  I want to connect to a mainframe host
  So that I can use the emulator with real applications

  Scenario: Successful connection and status update
    Given a mock mainframe server
    When I initiate a connection to the mock server
    Then the session status should be "CONNECTED"
    And I should be able to disconnect
