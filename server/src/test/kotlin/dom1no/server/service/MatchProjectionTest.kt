package dom1no.server.service

import dom1no.server.domain.BoardEnd
import dom1no.server.domain.EndReason
import dom1no.server.domain.MatchConfiguration
import dom1no.server.domain.MatchStatus
import dom1no.server.domain.ParticipantOutcome
import dom1no.server.domain.RejectionReason
import dom1no.server.domain.Tile
import dom1no.server.domain.deckOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Player-specific projection: a participant sees their own hand, public board and match state,
 * and only the sizes of other hands. These tests exercise all three configurations.
 */
class MatchProjectionTest {

    /** Seat 1 opens with 6|6 in every configuration; the rest of the deck is dealt in order. */
    private val deck = deckOf(
        "66 65 54 43 32 21 10 60 00 55 44 33 22 11 " +
            "02 03 04 05 13 14 15 16 24 25 26 35 36 46",
    )

    /** Hands after seat 1's opening tile has left its hand. */
    private val expectedHands = listOf(
        deckOf("65 54 43 32 21 10"),
        deckOf("60 00 55 44 33 22 11"),
        deckOf("02 03 04 05 13 14 15"),
        deckOf("16 24 25 26 35 36 46"),
    )

    private val twoPlayerEmptyHandScript = """
        p1 60 R
        p0 65 L
        p1 00 R
        p0 54 L
        p1 44 L
        p0 43 L
        p1 33 L
        p0 32 L
        p1 22 L
        p0 21 L
        p1 11 L
        p0 10 L
    """

    /** Seat 3 empties its hand after legal placements, draws and passes. */
    private val threePlayerEmptyHandDeck = deckOf(
        "33 25 14 36 66 06 05 15 23 13 46 16 34 24 55 01 22 02 26 56 04 44 11 12 45 00 03 35",
    )

    private val threePlayerEmptyHandScript = """
        p1 16 L
        p2 01 L
        p0 36 R
        p1 34 R
        p2 04 L
        p0 14 L
        p1 46 R
        p2 26 R
        p0 25 R
        p1 13 L
        p2 55 R
        p0 33 L
        p1 23 L
        p2 22 L
        p0 05 R
        p1 24 L
        p2 02 R
        d0 44
        p0 44 L
        d1 11,12
        p1 12 R
        d2 45
        p2 45 L
        d0 00,03,35
        p0 35 L
        p1 15 R
        p2 56 R
    """

    /** Seat 1 empties its hand, which wins the match for team 1. */
    private val partnershipEmptyHandDeck = deckOf(
        "33 25 14 36 66 06 05 15 23 13 46 16 34 24 55 01 22 02 26 56 04 44 11 12 45 00 03 35",
    )

    private val partnershipEmptyHandScript = """
        p1 16 L
        p2 01 L
        p3 00 L
        p0 05 L
        p1 46 R
        p2 56 L
        p3 45 R
        p0 25 R
        p1 23 R
        p2 26 L
        p3 12 L
        p0 33 R
        p1 13 L
        x2
        p3 03 L
        p0 06 L
        p1 34 R
        p2 04 R
        x3
        p0 36 L
        x1
        p2 02 R
        p3 35 L
        x0
        p1 24 R
        p2 55 L
        p3 44 R
        p0 14 R
    """

    /** Ends 5|5 with seat 1 holding 22 pips and seat 2 holding only 0|0. */
    private val twoPlayerBlockedDeck = deckOf(
        "36 55 13 05 25 46 03 35 06 14 02 23 00 34 " +
            "01 24 04 11 15 16 44 22 33 66 12 56 26 45",
    )

    private val twoPlayerBlockedScript = """
        p1 35 L
        p0 36 L
        p1 06 L
        p0 03 L
        p1 34 L
        p0 05 R
        p1 02 R
        p0 46 L
        p1 23 R
        p0 13 R
        p1 14 R
        d0 01,24
        p0 24 R
        d1 04,11,15,16
        p1 16 L
        p0 25 R
        p1 15 L
        d0 44,22,33,66,12,56
        p0 56 L
        d1 26
        p1 26 L
        p0 12 L
        p1 11 L
        p0 01 L
        p1 04 L
        p0 44 L
        d1 45
        p1 45 L
        x0
        x1
    """

    @Test
    fun `each configuration projects only the caller's hand and public state`() {
        for (configuration in MatchConfiguration.entries) {
            val service = MatchService()
            val participants = (1..configuration.seatCount).map { "player$it" }
            service.startMatch(configuration.name, configuration, participants, deck)
            val expectedSizes = expectedHands.take(configuration.seatCount).map { it.size }
            val expectedBoneyard = 28 - configuration.seatCount * 7

            for ((index, participant) in participants.withIndex()) {
                val view = requireNotNull(service.view(configuration.name, participant))
                val otherTiles = expectedHands.take(configuration.seatCount)
                    .filterIndexed { otherIndex, _ -> otherIndex != index }
                    .flatten()

                assertEquals(index + 1, view.seatNumber, "$configuration seat")
                assertEquals(expectedHands[index], view.hand, "$configuration own hand")
                assertTrue(view.hand.none { it in otherTiles }, "$configuration leaked another hand")
                assertEquals(expectedSizes, view.handCounts, "$configuration hand counts")
                assertEquals(participants, view.seats.map { it.participantId })
                assertEquals(
                    (1..configuration.seatCount).map { configuration.teamNumberFor(it) },
                    view.seats.map { it.teamNumber },
                )
                assertEquals(6, view.board.openLeft)
                assertEquals(6, view.board.openRight)
                assertEquals(2, view.activeSeatNumber)
                assertEquals(MatchStatus.ACTIVE, view.status)
                assertNull(view.result)
                assertEquals(0, view.version)
                assertEquals(expectedBoneyard, view.boneyardCount)
            }
        }
    }

    @Test
    fun `a finished individual match projects its result and final state to every participant`() {
        val service = MatchService()
        val participants = listOf("alice", "bob")
        service.startMatch("game", MatchConfiguration.TWO_PLAYER_INDIVIDUAL, participants, deck)

        val finalVersion = replay(service, "game", participants, twoPlayerEmptyHandScript)
        val views = participants.map { requireNotNull(service.view("game", it)) }

        for (view in views) {
            assertEquals(MatchStatus.FINISHED, view.status)
            assertEquals(EndReason.EMPTY_HAND, view.result?.endReason)
            assertEquals(
                mapOf(1 to ParticipantOutcome.WIN, 2 to ParticipantOutcome.LOSS),
                view.result?.outcomes,
            )
            assertEquals(setOf(1), view.result?.winners)
            assertTrue(view.result?.remainingPips?.isEmpty() == true)
            assertEquals(finalVersion, view.version)
            assertEquals(1, view.activeSeatNumber)
            assertEquals(13, view.board.placedTiles.size)
            assertEquals(view.hand.size, view.handCounts[view.seatNumber - 1])
        }
        assertEquals(emptyList<Tile>(), views[0].hand)
        assertEquals(deckOf("55"), views[1].hand)
        assertEquals(listOf(0, 1), views[0].handCounts)
    }

    @Test
    fun `a finished three-player match projects the winner without exposing other hands`() {
        val service = MatchService()
        val participants = listOf("alice", "bob", "carol")
        service.startMatch("game", MatchConfiguration.THREE_PLAYER_INDIVIDUAL, participants, threePlayerEmptyHandDeck)

        val finalVersion = replay(service, "game", participants, threePlayerEmptyHandScript)
        val views = participants.map { requireNotNull(service.view("game", it)) }

        for (view in views) {
            assertEquals(MatchStatus.FINISHED, view.status)
            assertEquals(EndReason.EMPTY_HAND, view.result?.endReason)
            assertEquals(
                mapOf(
                    1 to ParticipantOutcome.LOSS,
                    2 to ParticipantOutcome.LOSS,
                    3 to ParticipantOutcome.WIN,
                ),
                view.result?.outcomes,
            )
            assertEquals(setOf(3), view.result?.winners)
            assertEquals(finalVersion, view.version)
            assertEquals(3, view.activeSeatNumber)
            assertEquals(view.hand.size, view.handCounts[view.seatNumber - 1])
        }
        assertEquals(emptyList<Tile>(), views[2].hand)
        assertTrue(views[0].hand.isNotEmpty() && views[1].hand.isNotEmpty())
    }

    @Test
    fun `a finished partnership match projects the team result to both teams`() {
        val service = MatchService()
        val participants = listOf("a", "b", "c", "d")
        service.startMatch("game", MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, participants, partnershipEmptyHandDeck)

        val finalVersion = replay(service, "game", participants, partnershipEmptyHandScript)
        val views = participants.map { requireNotNull(service.view("game", it)) }

        for (view in views) {
            assertEquals(MatchStatus.FINISHED, view.status)
            assertEquals(EndReason.EMPTY_HAND, view.result?.endReason)
            assertEquals(
                mapOf(
                    1 to ParticipantOutcome.WIN,
                    2 to ParticipantOutcome.LOSS,
                    3 to ParticipantOutcome.WIN,
                    4 to ParticipantOutcome.LOSS,
                ),
                view.result?.outcomes,
            )
            assertEquals(setOf(1, 3), view.result?.winners)
            assertEquals(listOf(1, 2, 1, 2), view.seats.map { it.teamNumber })
            assertEquals(finalVersion, view.version)
            assertEquals(view.hand.size, view.handCounts[view.seatNumber - 1])
        }
        assertEquals(emptyList<Tile>(), views[0].hand)
    }

    @Test
    fun `a blocked match projects remaining pips and the blocked result`() {
        val service = MatchService()
        val participants = listOf("alice", "bob")
        service.startMatch("game", MatchConfiguration.TWO_PLAYER_INDIVIDUAL, participants, twoPlayerBlockedDeck)

        val finalVersion = replay(service, "game", participants, twoPlayerBlockedScript)
        val views = participants.map { requireNotNull(service.view("game", it)) }

        for (view in views) {
            assertEquals(MatchStatus.FINISHED, view.status)
            assertEquals(EndReason.BLOCKED, view.result?.endReason)
            assertEquals(mapOf(1 to 22, 2 to 0), view.result?.remainingPips)
            assertEquals(
                mapOf(1 to ParticipantOutcome.LOSS, 2 to ParticipantOutcome.WIN),
                view.result?.outcomes,
            )
            assertEquals(setOf(2), view.result?.winners)
            assertEquals(finalVersion, view.version)
            assertEquals(2, view.activeSeatNumber)
        }
        assertEquals(listOf(Tile(0, 0)), views[1].hand)
        assertEquals(0, views[1].result?.remainingPips?.get(2))
        assertTrue(views[0].hand.isNotEmpty())
    }

    @Test
    fun `rejected actions expose no match state and leave every view unchanged`() {
        val service = MatchService()
        val participants = listOf("player1", "player2", "player3")
        service.startMatch("game", MatchConfiguration.THREE_PLAYER_INDIVIDUAL, participants, deck)
        val before = participants.associateWith { requireNotNull(service.view("game", it)) }
        assertNull(service.view("game", "outsider"))

        assertRejected(ServiceRejection.NOT_PARTICIPANT) {
            service.place("game", "outsider", 0, Tile(6, 5), BoardEnd.LEFT)
        }
        assertRejected(ServiceRejection.NOT_PARTICIPANT) { service.draw("game", "outsider", 0) }
        assertRejected(ServiceRejection.NOT_PARTICIPANT) { service.pass("game", "outsider", 0) }
        assertRejected(ServiceRejection.Game(RejectionReason.NOT_YOUR_TURN)) {
            service.place("game", "player1", 0, Tile(6, 5), BoardEnd.LEFT)
        }
        assertRejected(ServiceRejection.STALE_VERSION) { service.draw("game", "player2", 99) }
        assertRejected(ServiceRejection.Game(RejectionReason.PLACEMENT_AVAILABLE)) {
            service.pass("game", "player2", 0)
        }

        participants.forEach { assertEquals(before[it], service.view("game", it)) }
        assertNull(service.view("game", "outsider"))
    }

    @Test
    fun `mutating projected snapshots and results cannot change later views`() {
        val service = MatchService()
        val participants = listOf("alice", "bob")
        service.startMatch("game", MatchConfiguration.TWO_PLAYER_INDIVIDUAL, participants, deck)
        replay(service, "game", participants, twoPlayerEmptyHandScript)

        val view = requireNotNull(service.view("game", "alice"))
        val hand = view.hand
        val counts = view.handCounts
        val board = view.board.placedTiles
        val seats = view.seats
        val result = requireNotNull(view.result)

        runCatching { (hand as MutableList<Tile>).clear() }
        runCatching { (counts as MutableList<Int>).clear() }
        runCatching { (board as MutableList<*>).clear() }
        runCatching { (seats as MutableList<MatchParticipant>).clear() }
        runCatching { (result.outcomes as MutableMap<Int, ParticipantOutcome>).clear() }

        val after = requireNotNull(service.view("game", "alice"))
        assertEquals(emptyList<Tile>(), after.hand)
        assertEquals(listOf(0, 1), after.handCounts)
        assertEquals(13, after.board.placedTiles.size)
        assertEquals(2, after.seats.size)
        assertEquals(
            mapOf(1 to ParticipantOutcome.WIN, 2 to ParticipantOutcome.LOSS),
            after.result?.outcomes,
        )
        assertEquals(MatchStatus.FINISHED, after.status)
    }

    /** Replays a recorded game through the service, asserting every action is accepted. */
    private fun replay(
        service: MatchService,
        matchId: String,
        participants: List<String>,
        script: String,
    ): Long {
        var version = 0L
        for (line in script.trim().lines()) {
            val parts = line.trim().split(Regex("\\s+"))
            if (parts[0].isEmpty()) continue
            val seatIndex = parts[0].drop(1).toInt()
            val participant = participants[seatIndex]
            val outcome = when (parts[0].first()) {
                'p' -> service.place(matchId, participant, version, tileOf(parts[1]), endOf(parts[2]))
                'd' -> service.draw(matchId, participant, version)
                'x' -> service.pass(matchId, participant, version)
                else -> error("Unknown action ${parts[0]}")
            }
            assertTrue(outcome is MatchActionResult.Accepted, "Expected accepted action: $line")
            version = (outcome as MatchActionResult.Accepted).state.version
        }
        return version
    }

    private fun assertRejected(reason: ServiceRejection, action: () -> MatchActionResult) {
        val rejected = action()
        assertTrue(rejected is MatchActionResult.Rejected)
        assertEquals(reason, (rejected as MatchActionResult.Rejected).reason)
    }

    private fun tileOf(token: String): Tile = Tile(token[0] - '0', token[1] - '0')

    private fun endOf(token: String): BoardEnd = when (token) {
        "L" -> BoardEnd.LEFT
        "R" -> BoardEnd.RIGHT
        else -> error("Unknown end $token")
    }
}
