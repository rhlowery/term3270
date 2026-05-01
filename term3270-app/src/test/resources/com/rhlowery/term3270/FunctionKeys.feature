Feature: Terminal Function Keys
  As a user
  I want to use UI buttons for 3270 functions
  So that I can interact with the host without keyboard shortcuts

  Scenario: Resetting the keyboard via UI
    Given a blank terminal buffer
    And the keyboard is locked
    When I click the "Reset" button
    Then the keyboard should be unlocked
