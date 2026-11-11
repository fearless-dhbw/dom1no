Feature: Send Friend Request
  As a signed-in player
  I want to find another player and send a pending friend request
  So that we can become friends if they later accept

  Scenario: Find a player by username
    Given I am signed in as "PLAYER1"
    And I am in the Friends section
    When I search for the username "PLAYER2"
    Then the system displays "PLAYER2" in the search results
    And I can view that player's information

  Scenario: Find a player using a mixed-case username substring
    Given I am signed in as "PLAYER1"
    And I am in the Friends section
    When I search for the username "lAyEr2"
    Then the system displays "PLAYER2" in the search results
    And I can view that player's information

  Scenario: Exclude my own account from matching search results
    Given I am signed in as "PLAYER1"
    And I am in the Friends section
    When I search for the username "pLaYeR"
    Then the system displays "PLAYER2" in the search results
    And the search results do not include "PLAYER1"

  Scenario: Search has no matching accounts
    Given I am signed in as "PLAYER1"
    And I am in the Friends section
    And no discoverable account matches "UNKNOWN_PLAYER"
    When I search for the username "UNKNOWN_PLAYER"
    Then the system displays an empty-result message
    And I can enter another username

  Scenario: Send a friend request
    Given I am signed in as "PLAYER1"
    And I have found the eligible account "PLAYER2" in the search results
    And we are not friends and no request is pending between us
    And request storage is available
    When I select "PLAYER2" and send a friend request
    Then one pending request from "PLAYER1" is available to "PLAYER2"
    And I see a confirmation that the friend request was sent
    And we have not yet been added to each other's friends lists

  Scenario: Attempt to add an existing friend
    Given I am signed in as "PLAYER1"
    And "PLAYER2" is already my friend
    When I attempt to send "PLAYER2" a friend request
    Then the system tells me "PLAYER2" is already my friend
    And no new friend request is created

  Scenario: Attempt to duplicate my pending request
    Given I am signed in as "PLAYER1"
    And a request from "PLAYER1" to "PLAYER2" is already pending
    When I attempt to send "PLAYER2" another friend request
    Then the system tells me my request is already pending
    And exactly one request from "PLAYER1" to "PLAYER2" remains pending

  Scenario: Reject a self-request
    Given I am signed in as "PLAYER1"
    When I attempt to send a friend request to my own account
    Then the system rejects the self-request
    And no request to my own account is created

  Scenario: Show an existing incoming request instead of sending a reciprocal request
    Given I am signed in as "PLAYER1"
    And a request from "PLAYER2" to "PLAYER1" is already pending
    When I attempt to send "PLAYER2" a friend request
    Then the system shows the existing incoming request with Accept and Decline choices
    And exactly one pending request remains between the two accounts
    And no friendship is created by the send attempt

  Scenario: Cancel before sending
    Given I am signed in as "PLAYER1"
    And I have selected "PLAYER2" without submitting a request
    When I leave the selection without sending
    Then no friend request is created

  Scenario: Saving fails before a pending request is created
    Given I am signed in as "PLAYER1"
    And I submitted an eligible friend request to "PLAYER2"
    When the service confirms a save failure before recording the request
    Then I see a failure message instead of a success confirmation
    And no pending request was created by this attempt

  Scenario: A save response is lost
    Given I am signed in as "PLAYER1"
    And I submitted an eligible friend request to "PLAYER2"
    When the response is lost before I receive confirmation
    Then I see an unconfirmed status instead of a success confirmation
    And the interface does not claim that no pending request exists
