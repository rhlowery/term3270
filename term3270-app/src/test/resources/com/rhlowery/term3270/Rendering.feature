Feature: Terminal Rendering
  As a user
  I want to see a correctly sized terminal screen
  So that I can interact with mainframe applications

  Scenario: Displaying the initial screen
    Given a connected terminal session
    When the UI is initialized
    Then the UI should render a 24x80 character grid
    And the default background color should be "BLACK"
