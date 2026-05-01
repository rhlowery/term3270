@issue-47
Feature: Exit Menu with Auto-Disconnect
  As a user
  I want to exit the application gracefully
  So that my sessions are properly disconnected

  Scenario: Exiting with an active session
    Given a connected session for exit testing
    When I trigger the "Exit" action
    Then the session should be disconnected
    And the application should terminate
