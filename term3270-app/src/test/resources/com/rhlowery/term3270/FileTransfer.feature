@issue-46
Feature: IND$FILE File Transfer
  As a user
  I want to transfer files between my terminal and the mainframe
  Using the standard IND$FILE protocol

  Scenario: Responding to a Query request
    When the host sends a "Read Partition (Query)" request
    Then the terminal should reply with a "Query Reply" including "File Transfer" support
    And the reply should contain the "0xCA" (File Transfer) attribute

  Scenario: Receiving file data via Structured Fields
    Given a file transfer is active for "DOWNLOAD.TXT"
    When the host sends a "Write Structured Field" with data "Hello Mainframe"
    And the host sends a "Write Structured Field" with a "Close" indicator
    Then the local file "DOWNLOAD.TXT" should contain "Hello Mainframe"

  Scenario: Uploading file data via Structured Fields
    Given a local file "UPLOAD.TXT" with content "Mainframe is Cool"
    When I upload the file "UPLOAD.TXT" with command "IND$FILE PUT UPLOAD.TXT"
    Then the terminal should send a Structured Field with data "Mainframe is Cool"

