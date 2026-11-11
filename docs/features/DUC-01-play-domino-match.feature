Feature: Play a Domino Match
  As a signed-in match participant
  I want to play one online domino game under the chosen configuration
  So that I can compete and understand the result

  # Joining precedes this goal: two or three individuals, or four fixed opposite-seat partners.
  # Each player starts with seven tiles from the double-six set; docs/game-rules.md is authoritative.

  Scenario Outline: Deal and open a full configured match
    Given a full <configuration> match is ready to start
    When the system shuffles and deals the 28 unique double-six tiles
    Then each participant receives seven tiles
    And the boneyard contains <stock> undealt tiles
    And the opener is selected by <opening_rule>
    And the opening tile leaves the opener's hand and starts the board
    And the next seat gets the turn with consecutive-pass count 0

    Examples:
      | configuration           | stock | opening_rule                                                               |
      | two-player individual   | 14    | highest dealt double, otherwise greatest pip total with highest-end tiebreak |
      | three-player individual | 7     | highest dealt double, otherwise greatest pip total with highest-end tiebreak |
      | four-player partnership | 0     | the holder of 6\|6 opening with 6\|6                                         |

  Scenario: Place a matching tile with the selected orientation
    Given I am in an initialized three-player individual game
    And it is my turn with open board ends of 6 and 2
    And my hand contains "6|3" and at least one other tile
    And the consecutive-pass count is 1
    When I place "6|3" with its 6 touching the open 6
    Then the new open ends are 3 and 2
    And "6|3" is removed from my hand
    And the consecutive-pass count is 0
    And the next seat gets the turn
    And all participants can receive the accepted state

  Scenario: Reject a tile that does not match the selected end
    Given I am in an initialized two-player individual game
    And it is my turn with open board ends of 6 and 2
    And my hand contains "5|4"
    When I try to place "5|4" on the open 6
    Then the system explains that the placement was rejected
    And the board, my hand and the active turn remain unchanged
    And the consecutive-pass count remains unchanged

  Scenario: Reject an out-of-turn placement
    Given I am in an initialized four-player partnership game
    And it is another player's turn
    When I submit a placement of "6|3"
    Then the system rejects my out-of-turn placement
    And the board, my hand and the active turn remain unchanged

  Scenario: Draw until a playable tile is obtained
    Given I am in an initialized two-player individual game
    And it is my turn with open board ends of 6 and 2
    And none of my tiles matches either open end
    And the next boneyard tiles in draw order are "5|4" and "6|1"
    When I draw because I cannot place a tile
    Then I draw "5|4" followed by "6|1" and stop drawing
    And both drawn tiles are added to my hand
    And it is still my turn to place "6|1"

  Scenario: Exhaust the boneyard without finding a legal placement
    Given I am in an initialized three-player individual game
    And it is my turn with open board ends of 6 and 2
    And none of my tiles matches either open end
    And the boneyard contains only "5|4"
    And the consecutive-pass count is 0
    When I draw "5|4" and still cannot place a tile
    Then the boneyard is empty and "5|4" stays in my hand
    And I pass and the next seat gets the turn
    And the consecutive-pass count is 1

  Scenario Outline: End a blocked partnership game after four consecutive passes
    Given I am in an initialized four-player partnership game with no empty hands
    And there is no boneyard
    And the previous three players have passed consecutively
    And the active player cannot place any tile
    And opposite-seat partners PLAYER1 and PLAYER3 have remaining pip totals 4 and 8
    And partners PLAYER2 and PLAYER4 have remaining pip totals <second> and <fourth>
    When the active player passes
    Then the game ends with four consecutive passes
    And the team totals displayed are 12 and <other_total>
    And the result is <result>

    Examples:
      | second | fourth | other_total | result                         |
      | 7      | 9      | 16          | a win for PLAYER1 and PLAYER3   |
      | 5      | 7      | 12          | a tie between the partnerships |

  Scenario Outline: Resolve a blocked individual game using remaining hand pips
    Given I am in an initialized <players>-player individual game with no empty hands
    And the boneyard is empty and no participant can place a tile
    And the consecutive-pass count is <previous_passes>
    And the remaining hand pip totals by seat are <totals>
    When the active player passes
    Then the game ends because the consecutive-pass count equals the player count
    And the displayed result is <result>

    Examples:
      | players | previous_passes | totals  | result                     |
      | 2       | 1               | 5, 9    | a win for seat 1           |
      | 3       | 2               | 5, 5, 8 | a tie between seats 1 and 2 |

  Scenario Outline: End immediately when a participant places their last tile
    Given I am in an initialized <configuration> game
    And it is <player>'s turn with only "6|3" left in their hand
    And "6|3" can be legally placed on the open 6
    When <player> places "6|3" with its 6 touching the open 6
    Then the game ends immediately with <player>'s hand empty
    And the displayed winner is <winner>
    And no participant gets another turn

    Examples:
      | configuration           | player  | winner                            |
      | two-player individual   | PLAYER1 | PLAYER1                           |
      | three-player individual | PLAYER2 | PLAYER2                           |
      | four-player partnership | PLAYER2 | PLAYER2 and opposite-seat PLAYER4 |

  Scenario: A placement is known not to have been sent
    Given it is my turn in an initialized online game
    When my placement cannot be sent to the game service
    Then the interface does not present it as an accepted move
    And it tells me the placement was not sent

  Scenario: A response is lost after a placement was sent
    Given I submitted a placement in an initialized online game
    When the connection is lost before its acceptance response arrives
    Then the interface shows that acceptance is unconfirmed
    And it does not assert that the authoritative board and turn are unchanged
