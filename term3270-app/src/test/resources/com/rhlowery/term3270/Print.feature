@issue-44
Feature: Print Screen and Capture
  As a user
  I want to capture the terminal screen contents
  So that I can save or share terminal output

  Background:
    Given a blank terminal buffer
    And I write "USERID ===> " at row 1 column 1
    And an unprotected field at row 1 column 13
    And I write "MYUSER" at row 1 column 14

  Scenario: Extracting screen as plain text
    When I request the screen as plain text
    Then the text should contain "USERID ===> " at the beginning
    And the text should contain "MYUSER" at position (1, 14)

  Scenario: Copying screen to clipboard
    When I copy the screen to the clipboard
    Then the system clipboard should contain the terminal text
