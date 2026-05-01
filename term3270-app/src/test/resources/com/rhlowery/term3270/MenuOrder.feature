Feature: Menu Bar Order
  As a user
  I want the File menu to appear before the Session menu
  So that the application follows standard UI conventions

  @issue-60
  Scenario: Verify menu order
    Given a terminal application is running
    Then the first menu should be "File"
    And the second menu should be "Session"
    And the third menu should be "Macros"
