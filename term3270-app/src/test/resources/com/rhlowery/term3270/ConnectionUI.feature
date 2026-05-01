Feature: Connection Management UI
  As a user
  I want to be able to configure and manage my session connections
  So that I can connect to different mainframe systems

  Scenario: Opening the Connection Dialog
    Given a terminal application is running
    When I select "Session" -> "Connect..." from the menu
    Then the connection dialog should be visible

  Scenario: Successful connection via UI
    Given a terminal application is running
    And a mock server is available
    And the connection dialog is open
    When I enter "localhost" as host and 3270 as port
    And I click "Connect"
    Then the session status should eventually be "CONNECTED"

  Scenario: Disconnecting via UI
    Given a terminal application is running
    And a connected terminal session
    When I select "Session" -> "Disconnect" from the menu
    Then the session status should be "DISCONNECTED"

  Scenario: Restarting the connection
    Given a terminal application is running
    And a connected terminal session
    When I select "Session" -> "Restart" from the menu
    Then the session should disconnect and then reconnect

  Scenario Outline: Connecting with secure options
    Given a terminal application is running
    And a mock server is available
    And the connection dialog is open
    When I enter "<host>" as host and <port> as port
    And I toggle secure connection to <secure>
    And I toggle verify hostname to <verify>
    And I click "Connect"
    Then the session status should eventually be "CONNECTED"

    Examples:
      | host      | port | secure | verify |
      | localhost | 3270 | true   | false  |
      | localhost | 3270 | false  | false  |

  Scenario: Selecting Emulation Type
    Given a terminal application is running
    And the connection dialog is open
    When I select "5250" as emulation type
    And I click "Connect"
    Then the session should use "5250" emulation

  Scenario Outline: Selecting Terminal Size
    Given a terminal application is running
    And the connection dialog is open
    When I select "<size>" as terminal size
    And I click "Connect"
    Then the session should be initialized with <cols> columns and <rows> rows

    Examples:
      | size   | cols | rows |
      | 80x24  | 80   | 24   |
      | 80x32  | 80   | 32   |
      | 80x43  | 80   | 43   |
      | 132x27 | 132  | 27   |
