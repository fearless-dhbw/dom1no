package dom1no.server.acceptance

import dom1no.server.domain.BoardEnd
import dom1no.server.domain.DominoSet
import dom1no.server.domain.EndReason
import dom1no.server.domain.MatchConfiguration
import dom1no.server.domain.MatchStatus
import dom1no.server.domain.ParticipantOutcome
import dom1no.server.domain.RejectionReason
import dom1no.server.domain.Tile
import dom1no.server.service.MatchActionResult
import dom1no.server.service.MatchService
import dom1no.server.service.MatchView
import dom1no.server.service.ServiceRejection
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Bindings for the domain-testable DUC-01 paths. This file is intentionally outside the current
 * Gradle test source set: DOM-46 owns Cucumber JVM/JUnit Platform configuration and the final
 * module layout. No delivery, authentication or UI behavior is faked here.
 */
class DUC01Steps {
    private val service = MatchService()
    private val matchId = "duc-01"
    private lateinit var configuration: MatchConfiguration
    private lateinit var participants: List<String>
    private lateinit var actor: String
    private lateinit var before: MatchView
    private lateinit var outcome: MatchActionResult
    private lateinit var initialDeck: List<Tile>
    private lateinit var scenarioName: String
    private lateinit var expectedPips: List<Int>
    private var deferredPartnership = false
    private var firstTeamExpected = Pair(0, 0)

    @Before
    fun scenario(scenario: Scenario) {
        scenarioName = scenario.name
    }

    @Given("^a full (two-player individual|three-player individual|four-player partnership) match is ready to start$")
    fun readyToStart(name: String) {
        configuration = configurationOf(name)
        participants = (1..configuration.seatCount).map { "PLAYER$it" }
        // A fixed, already-shuffled deck exercises dealing and opening; shuffle randomness is
        // covered separately by MatchSetupTest. For two players 6|6 is in stock and 5|5 opens.
        initialDeck = deckWith(
            "55 06 05 04 03 02 01 44 16 15 14 13 12 11 " +
                "66 26 25 24 23 22 34",
        )
    }

    @When("the system shuffles and deals the 28 unique double-six tiles")
    fun start() {
        service.startMatch(matchId, configuration, participants, initialDeck)
    }

    @Then("each participant receives seven tiles")
    fun sevenTilesEach() {
        val views = participants.map { view(it) }
        val dealtHands = views.mapIndexed { index, state ->
            state.hand + if (index + 1 == openerSeat()) listOf(state.board.placedTiles.single().tile) else emptyList()
        }
        assertTrue(dealtHands.all { it.size == 7 })
        assertEquals(initialDeck.take(participants.size * 7).toSet(), dealtHands.flatten().toSet())
        assertEquals(28, (dealtHands.flatten() + initialDeck.drop(participants.size * 7)).toSet().size)
    }

    @Then("^the boneyard contains (\\d+) undealt tiles$")
    fun stockSize(expected: Int) {
        assertEquals(expected, view(participants.first()).boneyardCount)
    }

    @Then("^the opener is selected by (.+)$")
    fun openingRule(rule: String) {
        val expected = if (configuration == MatchConfiguration.TWO_PLAYER_INDIVIDUAL) Tile(5, 5) else Tile(6, 6)
        assertEquals(expected, view(participants.first()).board.placedTiles.single().tile)
        assertEquals(if (configuration == MatchConfiguration.TWO_PLAYER_INDIVIDUAL) 1 else 3, openerSeat())
        assertTrue(rule.contains(if (configuration.isPartnership) "6|6" else "highest dealt double"))
    }

    @Then("the opening tile leaves the opener's hand and starts the board")
    fun openingPlacement() {
        val state = view(participants[openerSeat() - 1])
        assertEquals(1, state.board.placedTiles.size)
        assertFalse(state.hand.contains(state.board.placedTiles.single().tile))
        assertEquals(6, state.hand.size)
    }

    @Then("the next seat gets the turn with consecutive-pass count 0")
    fun initialTurn() {
        val state = view(participants.first())
        assertEquals(openerSeat() % participants.size + 1, state.activeSeatNumber)
        assertEquals(0, state.consecutivePasses)
        assertEquals(0, state.version)
    }

    @Given("^I am in an initialized (two-player individual|three-player individual|four-player partnership) game$")
    fun initializedGame(name: String) {
        if (scenarioName.startsWith("End immediately when")) {
            val configuration = configurationOf(name)
            val game = when (configuration) {
                MatchConfiguration.TWO_PLAYER_INDIVIDUAL -> DUC01Games.emptyTwoPlayers
                MatchConfiguration.THREE_PLAYER_INDIVIDUAL -> DUC01Games.emptyThreePlayers
                MatchConfiguration.FOUR_PLAYER_PARTNERSHIP -> DUC01Games.emptyPartnership
            }
            startRecorded(configuration, game)
        } else {
            when (configurationOf(name)) {
                MatchConfiguration.TWO_PLAYER_INDIVIDUAL -> initializedTwoPlayerGame()
                MatchConfiguration.THREE_PLAYER_INDIVIDUAL -> initializedThreePlayerGame()
                MatchConfiguration.FOUR_PLAYER_PARTNERSHIP -> initializedPartnershipGame()
            }
        }
    }

    fun initializedTwoPlayerGame() {
        configuration = MatchConfiguration.TWO_PLAYER_INDIVIDUAL
        participants = listOf("PLAYER1", "PLAYER2")
        actor = "PLAYER1"
        initialDeck = if (scenarioName == "Draw until a playable tile is obtained") {
            deckWith("66 00 11 33 44 55 13 62 06 15 25 36 46 56 54 61")
        } else {
            deckWith("66 54 55 44 33 11 00 62 06 16 25 36 46 56")
        }
        service.startMatch(matchId, configuration, participants, initialDeck)
        accepted(service.place(matchId, "PLAYER2", 0, Tile(6, 2), BoardEnd.RIGHT))
    }

    fun initializedPartnershipGame() {
        configuration = MatchConfiguration.FOUR_PLAYER_PARTNERSHIP
        participants = (1..4).map { "PLAYER$it" }
        actor = "PLAYER2"
        val deck = deckWith(
            "55 00 11 22 33 44 05 63 01 12 13 14 15 16 " +
                "66 02 03 04 06 23 24",
        )
        service.startMatch(matchId, configuration, participants, deck)
    }

    private fun initializedThreePlayerGame() {
        val placement = scenarioName == "Place a matching tile with the selected orientation"
        startRecorded(
            MatchConfiguration.THREE_PLAYER_INDIVIDUAL,
            if (placement) DUC01Games.placementAfterPass else DUC01Games.stockExhaustion,
        )
        actor = if (placement) "PLAYER1" else "PLAYER3"
    }

    @Given("I am in an initialized four-player partnership game with no empty hands")
    fun blockedPartnership() {
        configuration = MatchConfiguration.FOUR_PLAYER_PARTNERSHIP
        participants = (1..4).map { "PLAYER$it" }
        deferredPartnership = true
    }

    @Given("^I am in an initialized (\\d+)-player individual game with no empty hands$")
    fun blockedIndividual(players: Int) {
        assertTrue(players == 2 || players == 3)
        startRecorded(
            if (players == 2) MatchConfiguration.TWO_PLAYER_INDIVIDUAL else MatchConfiguration.THREE_PLAYER_INDIVIDUAL,
            if (players == 2) DUC01Games.blockedTwoPlayers else DUC01Games.blockedThreePlayers,
        )
        assertTrue(participants.all { view(it).hand.isNotEmpty() })
    }

    @Given("it is my turn with open board ends of 6 and 2")
    fun openEnds() {
        before = view(actor)
        assertEquals(6, before.board.openLeft)
        assertEquals(2, before.board.openRight)
        assertEquals(before.seatNumber, before.activeSeatNumber)
    }

    @Given("my hand contains {string}")
    fun hasTile(tile: String) {
        assertTrue(view(actor).hand.contains(tileOf(tile)))
    }

    @Given("it is another player's turn")
    fun anotherTurn() {
        before = view(actor)
        assertTrue(before.activeSeatNumber != before.seatNumber)
    }

    @When("I try to place {string} on the open 6")
    fun rejectIllegalPlacement(tile: String) {
        outcome = service.place(matchId, actor, view(actor).version, tileOf(tile), BoardEnd.LEFT)
    }

    @When("I submit a placement of {string}")
    fun outOfTurnPlacement(tile: String) {
        outcome = service.place(matchId, actor, view(actor).version, tileOf(tile), BoardEnd.LEFT)
    }

    @Then("the system explains that the placement was rejected")
    fun illegalPlacementRejected() {
        assertEquals(MatchActionResult.Rejected(ServiceRejection.Game(RejectionReason.ILLEGAL_PLACEMENT)), outcome)
    }

    @Then("the system rejects my out-of-turn placement")
    fun outOfTurnRejected() {
        assertEquals(MatchActionResult.Rejected(ServiceRejection.Game(RejectionReason.NOT_YOUR_TURN)), outcome)
    }

    @Then("the board, my hand and the active turn remain unchanged")
    fun rejectedStateUnchanged() {
        assertEquals(before, view(actor))
    }

    @Then("the consecutive-pass count remains unchanged")
    fun rejectedPassesUnchanged() {
        assertEquals(before.consecutivePasses, view(actor).consecutivePasses)
    }

    @Given("none of my tiles matches either open end")
    fun noPlacement() {
        val state = view(actor)
        assertTrue(state.hand.none { it.matches(state.board.openLeft!!) || it.matches(state.board.openRight!!) })
    }

    @Given("the next boneyard tiles in draw order are {string} and {string}")
    fun nextStockTiles(first: String, second: String) {
        assertEquals(listOf(tileOf(first), tileOf(second)), initialDeck.drop(14).take(2))
    }

    @When("I draw because I cannot place a tile")
    fun draw() {
        outcome = service.draw(matchId, actor, view(actor).version)
    }

    @Then("I draw {string} followed by {string} and stop drawing")
    fun drawnTiles(first: String, second: String) {
        assertEquals(listOf(tileOf(first), tileOf(second)), accepted(outcome).drawnTiles)
    }

    @Then("both drawn tiles are added to my hand")
    fun drawnTilesInHand() {
        val accepted = accepted(outcome)
        assertTrue(accepted.state.hand.containsAll(accepted.drawnTiles))
        assertEquals(before.hand.size + accepted.drawnTiles.size, accepted.state.hand.size)
    }

    @Then("it is still my turn to place {string}")
    fun retainedTurn(tile: String) {
        val state = accepted(outcome).state
        assertEquals(state.seatNumber, state.activeSeatNumber)
        assertTrue(state.hand.contains(tileOf(tile)))
        assertEquals(before.version + 1, state.version)
    }

    @Given("my hand contains {string} and at least one other tile")
    fun tileAndAnother(tile: String) {
        hasTile(tile)
        assertTrue(view(actor).hand.size > 1)
    }

    @Given("^the consecutive-pass count is (\\d+)$")
    fun passCount(expected: Int) {
        val state = if (::outcome.isInitialized && outcome is MatchActionResult.Accepted) {
            accepted(outcome).state
        } else {
            view(participants.first())
        }
        assertEquals(expected, state.consecutivePasses)
    }

    @When("I place {string} with its 6 touching the open 6")
    fun place(tile: String) {
        outcome = service.place(matchId, actor, before.version, tileOf(tile), BoardEnd.LEFT)
    }

    @Then("^the new open ends are (\\d+) and (\\d+)$")
    fun newEnds(left: Int, right: Int) {
        assertEquals(left, accepted(outcome).state.board.openLeft)
        assertEquals(right, accepted(outcome).state.board.openRight)
    }

    @Then("{string} is removed from my hand")
    fun removedFromHand(tile: String) {
        assertFalse(accepted(outcome).state.hand.contains(tileOf(tile)))
        assertEquals(before.hand.size - 1, accepted(outcome).state.hand.size)
    }

    @Then("the next seat gets the turn")
    fun nextSeat() {
        assertEquals(before.seatNumber % participants.size + 1, accepted(outcome).state.activeSeatNumber)
    }

    @Then("all participants can receive the accepted state")
    fun perParticipantViews() {
        val state = accepted(outcome).state
        for (participant in participants) {
            val projected = view(participant)
            assertEquals(state.version, projected.version)
            assertEquals(state.board, projected.board)
            assertEquals(state.handCounts, projected.handCounts)
            assertEquals(state.activeSeatNumber, projected.activeSeatNumber)
        }
    }

    @Given("the boneyard contains only {string}")
    fun oneTileLeft(tile: String) {
        assertEquals(1, view(actor).boneyardCount)
        assertEquals(tileOf(tile), initialDeck.last())
    }

    @When("I draw {string} and still cannot place a tile")
    fun drawLastTile(tile: String) {
        before = view(actor)
        outcome = service.draw(matchId, actor, before.version)
        assertEquals(listOf(tileOf(tile)), accepted(outcome).drawnTiles)
        noPlacement()
    }

    @Then("the boneyard is empty and {string} stays in my hand")
    fun exhaustedStock(tile: String) {
        val state = accepted(outcome).state
        assertEquals(0, state.boneyardCount)
        assertTrue(state.hand.contains(tileOf(tile)))
    }

    @Then("I pass and the next seat gets the turn")
    fun passAfterDraw() {
        val state = accepted(outcome).state
        outcome = service.pass(matchId, actor, state.version)
        assertEquals(state.seatNumber % participants.size + 1, accepted(outcome).state.activeSeatNumber)
    }

    @Given("there is no boneyard")
    fun noStock() {
        if (!deferredPartnership) assertEquals(0, view(participants.first()).boneyardCount)
    }

    @Given("the previous three players have passed consecutively")
    fun threePasses() {
        if (!deferredPartnership) assertEquals(3, view(participants.first()).consecutivePasses)
    }

    @Given("the active player cannot place any tile")
    fun activeCannotPlace() {
        if (deferredPartnership) return
        val state = view(participants.first())
        actor = participants[state.activeSeatNumber - 1]
        noPlacement()
    }

    @Given("^opposite-seat partners PLAYER1 and PLAYER3 have remaining pip totals (\\d+) and (\\d+)$")
    fun firstTeamPips(first: Int, third: Int) {
        if (deferredPartnership) {
            firstTeamExpected = Pair(first, third)
            return
        }
        assertEquals(first, view("PLAYER1").hand.sumOf { it.pipSum() })
        assertEquals(third, view("PLAYER3").hand.sumOf { it.pipSum() })
    }

    @Given("^partners PLAYER2 and PLAYER4 have remaining pip totals (\\d+) and (\\d+)$")
    fun secondTeamPips(second: Int, fourth: Int) {
        assertTrue(deferredPartnership)
        startRecorded(
            MatchConfiguration.FOUR_PLAYER_PARTNERSHIP,
            if (second == 7 && fourth == 9) DUC01Games.blockedTeamWin else DUC01Games.blockedTeamTie,
        )
        deferredPartnership = false
        assertTrue(participants.all { view(it).hand.isNotEmpty() })
        noStock()
        threePasses()
        activeCannotPlace()
        firstTeamPips(firstTeamExpected.first, firstTeamExpected.second)
        assertEquals(second, view("PLAYER2").hand.sumOf { it.pipSum() })
        assertEquals(fourth, view("PLAYER4").hand.sumOf { it.pipSum() })
    }

    @Given("the boneyard is empty and no participant can place a tile")
    fun blockedIndividualPosition() {
        noStock()
        val board = view(participants.first()).board
        assertTrue(participants.all { participant ->
            view(participant).hand.none { tile ->
                tile.matches(board.openLeft!!) || tile.matches(board.openRight!!)
            }
        })
    }

    @Given("^the remaining hand pip totals by seat are ([\\d, ]+)$")
    fun pipTotals(totals: String) {
        expectedPips = totals.split(",").map { it.trim().toInt() }
        assertEquals(expectedPips, participants.map { view(it).hand.sumOf { tile -> tile.pipSum() } })
    }

    @When("the active player passes")
    fun finalPass() {
        val active = view(participants.first()).activeSeatNumber
        actor = participants[active - 1]
        before = view(actor)
        outcome = service.pass(matchId, actor, before.version)
    }

    @Then("the game ends with four consecutive passes")
    fun partnershipBlocked() {
        assertEquals(MatchStatus.FINISHED, accepted(outcome).state.status)
        assertEquals(4, accepted(outcome).state.consecutivePasses)
        assertEquals(EndReason.BLOCKED, accepted(outcome).state.result?.endReason)
    }

    @Then("^the team totals displayed are (\\d+) and (\\d+)$")
    fun teamTotals(first: Int, second: Int) {
        val pips = requireNotNull(accepted(outcome).state.result).remainingPips
        assertEquals(first, pips.getValue(1) + pips.getValue(3))
        assertEquals(second, pips.getValue(2) + pips.getValue(4))
    }

    @Then("the game ends because the consecutive-pass count equals the player count")
    fun individualBlocked() {
        assertEquals(MatchStatus.FINISHED, accepted(outcome).state.status)
        assertEquals(participants.size, accepted(outcome).state.consecutivePasses)
        assertEquals(EndReason.BLOCKED, accepted(outcome).state.result?.endReason)
        assertEquals(
            expectedPips,
            participants.indices.map { requireNotNull(accepted(outcome).state.result).remainingPips.getValue(it + 1) },
        )
    }

    @Then("^the (?:displayed )?result is (.+)$")
    fun result(description: String) {
        val actual = requireNotNull(accepted(outcome).state.result)
        when (description) {
            "a win for PLAYER1 and PLAYER3" -> assertEquals(setOf(1, 3), actual.winners)
            "a tie between the partnerships" -> {
                assertTrue(actual.isDraw)
                assertTrue(actual.outcomes.values.all { it == ParticipantOutcome.DRAW })
            }
            "a win for seat 1" -> assertEquals(setOf(1), actual.winners)
            "a tie between seats 1 and 2" -> {
                assertTrue(actual.isDraw)
                assertEquals(ParticipantOutcome.DRAW, actual.outcomes[1])
                assertEquals(ParticipantOutcome.DRAW, actual.outcomes[2])
                assertEquals(ParticipantOutcome.LOSS, actual.outcomes[3])
            }
            else -> error("Unknown expected result $description")
        }
    }

    @Given("it is {word}'s turn with only {string} left in their hand")
    fun lastTile(player: String, tile: String) {
        actor = player
        before = view(actor)
        assertEquals(before.seatNumber, before.activeSeatNumber)
        assertEquals(listOf(tileOf(tile)), before.hand)
    }

    @Given("{string} can be legally placed on the open 6")
    fun lastTileMatches(tile: String) {
        assertEquals(6, before.board.openRight)
        assertTrue(tileOf(tile).matches(before.board.openRight!!))
    }

    @When("{word} places {string} with its 6 touching the open 6")
    fun lastPlacement(player: String, tile: String) {
        assertEquals(actor, player)
        outcome = service.place(matchId, actor, before.version, tileOf(tile), BoardEnd.RIGHT)
    }

    @Then("the game ends immediately with {word}'s hand empty")
    fun emptiedHand(player: String) {
        val state = accepted(outcome).state
        assertEquals(player, actor)
        assertTrue(state.hand.isEmpty())
        assertEquals(MatchStatus.FINISHED, state.status)
        assertEquals(EndReason.EMPTY_HAND, state.result?.endReason)
    }

    @Then("the displayed winner is {word}")
    fun individualWinner(player: String) {
        assertEquals(setOf(participants.indexOf(player) + 1), accepted(outcome).state.result?.winners)
    }

    @Then("the displayed winner is PLAYER2 and opposite-seat PLAYER4")
    fun partnershipWinner() {
        assertEquals(setOf(2, 4), accepted(outcome).state.result?.winners)
    }

    @Then("no participant gets another turn")
    fun noFurtherTurns() {
        val finished = accepted(outcome).state
        assertEquals(before.seatNumber, finished.activeSeatNumber)
        for (participant in participants) {
            val state = view(participant)
            assertEquals(finished.version, state.version)
            assertEquals(
                MatchActionResult.Rejected(ServiceRejection.Game(RejectionReason.MATCH_NOT_ACTIVE)),
                service.pass(matchId, participant, finished.version),
            )
        }
        assertEquals(finished, view(actor))
    }

    private fun startRecorded(configuration: MatchConfiguration, game: RecordedGame) {
        this.configuration = configuration
        participants = (1..configuration.seatCount).map { "PLAYER$it" }
        initialDeck = game.deck.trim().split(Regex("\\s+")).map { Tile(it[0] - '0', it[1] - '0') }
        service.startMatch(matchId, configuration, participants, initialDeck)
        for (line in game.prelude.trim().lines()) {
            val parts = line.trim().split(Regex("\\s+"))
            val participant = participants[parts[0].drop(1).toInt()]
            val version = view(participant).version
            val transition = when (parts[0].first()) {
                'p' -> service.place(matchId, participant, version, Tile(parts[1][0] - '0', parts[1][1] - '0'),
                    if (parts[2] == "L") BoardEnd.LEFT else BoardEnd.RIGHT)
                'd' -> service.draw(matchId, participant, version).also { result ->
                    assertEquals(parts[1].split(",").map { Tile(it[0] - '0', it[1] - '0') }, accepted(result).drawnTiles)
                }
                'x' -> service.pass(matchId, participant, version)
                else -> error("Unknown recorded action $line")
            }
            accepted(transition)
        }
    }

    private fun view(participant: String): MatchView =
        requireNotNull(service.view(matchId, participant))

    private fun openerSeat(): Int =
        (1..participants.size).single { view(participants[it - 1]).hand.size == 6 }

    private fun accepted(result: MatchActionResult): MatchActionResult.Accepted {
        assertTrue(result is MatchActionResult.Accepted, "Expected accepted action, got $result")
        return result as MatchActionResult.Accepted
    }

    private fun configurationOf(name: String): MatchConfiguration = when (name) {
        "two-player individual" -> MatchConfiguration.TWO_PLAYER_INDIVIDUAL
        "three-player individual" -> MatchConfiguration.THREE_PLAYER_INDIVIDUAL
        "four-player partnership" -> MatchConfiguration.FOUR_PLAYER_PARTNERSHIP
        else -> error("Unknown configuration $name")
    }

    private fun tileOf(text: String) = Tile(text[0] - '0', text[2] - '0')

    private fun deckWith(prefix: String): List<Tile> {
        val first = prefix.split(" ").map { Tile(it[0] - '0', it[1] - '0') }
        return first + DominoSet.standardDoubleSix().filterNot { it in first }
    }
}
