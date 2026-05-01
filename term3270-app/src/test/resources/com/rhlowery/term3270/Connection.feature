Feature: Mainframe Connection
  As a systems programmer
  I want to connect to a mainframe host using TN3270
  So that I can interact with z/OS or z/VM applications

  Scenario Outline: Successful connection to a host
    Given a mainframe host "<host>" on port <port>
    And a terminal type of "<term_type>"
    When I attempt to connect
    Then the session status should be "CONNECTED"
    And the terminal screen should be initialized

    Examples:
      | host           | port | term_type |
      | hercules.local | 3270 | IBM-3278-2|
      | zos.test.com   | 23   | IBM-3278-4|
