package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchDrawPassTest {

    /** Seat 1 opens with 6|6; seat 2 has no 6, and the boneyard draws 0|3, 0|4 then playable 0|6. */
    private val drawDeck = listOf(
        Tile(6, 6), Tile(6, 5), Tile(5, 4), Tile(4, 3), Tile(3, 2), Tile(2, 1), Tile(1, 0),
        Tile(0, 0), Tile(1, 1), Tile(2, 2), Tile(3, 3), Tile(4, 4), Tile(5, 5), Tile(0, 2),
        Tile(0, 3), Tile(0, 4), Tile(0, 6), Tile(0, 5), Tile(1, 3), Tile(1, 4), Tile(1, 5),
        Tile(1, 6), Tile(2, 4), Tile(2, 5), Tile(2, 6), Tile(3, 5), Tile(3, 6), Tile(4, 6),
    )

    /** Seat 1 opens with 0|0; seat 2 and the boneyard hold no 0, so drawing never helps. */
    private val blockedDeck = listOf(
        Tile(0, 0), Tile(0, 1), Tile(0, 2), Tile(0, 3), Tile(0, 4), Tile(0, 5), Tile(0, 6),
        Tile(1, 2), Tile(1, 3), Tile(1, 4), Tile(1, 5), Tile(1, 6), Tile(2, 3), Tile(2, 4),
        Tile(1, 1), Tile(2, 2), Tile(3, 3), Tile(4, 4), Tile(5, 5), Tile(6, 6), Tile(2, 5),
        Tile(2, 6), Tile(3, 4), Tile(3, 5), Tile(3, 6), Tile(4, 5), Tile(4, 6), Tile(5, 6),
    )

    /** Seat 1 opens with 0|0; seats 2 and 3 and the boneyard hold no 0. */
    private val threePlayerDeck = listOf(
        Tile(0, 0), Tile(0, 1), Tile(0, 2), Tile(0, 3), Tile(0, 4), Tile(0, 5), Tile(0, 6),
        Tile(1, 2), Tile(1, 3), Tile(1, 4), Tile(1, 5), Tile(1, 6), Tile(2, 3), Tile(2, 4),
        Tile(2, 5), Tile(2, 6), Tile(3, 4), Tile(3, 5), Tile(3, 6), Tile(4, 5), Tile(4, 6),
        Tile(1, 1), Tile(2, 2), Tile(3, 3), Tile(4, 4), Tile(5, 5), Tile(6, 6), Tile(5, 6),
    )

    /** Seat 3 opens with 6|6; seat 4 has no 6 and there is no boneyard. */
    private val partnershipNoSixDeck = listOf(
        Tile(0, 6), Tile(1, 6), Tile(2, 6), Tile(0, 0), Tile(1, 1), Tile(2, 2), Tile(3, 3),
        Tile(3, 6), Tile(4, 6), Tile(5, 6), Tile(4, 4), Tile(5, 5), Tile(0, 1), Tile(0, 2),
        Tile(6, 6), Tile(0, 3), Tile(0, 4), Tile(0, 5), Tile(1, 2), Tile(1, 3), Tile(1, 4),
        Tile(1, 5), Tile(2, 3), Tile(2, 4), Tile(2, 5), Tile(3, 4), Tile(3, 5), Tile(4, 5),
    )

    /** Seat 3 opens with 6|6; seat 4 holds 5|6 and therefore has a legal placement. */
    private val partnershipPlayableDeck = listOf(
        Tile(0, 1), Tile(0, 2), Tile(0, 3), Tile(0, 4), Tile(0, 5), Tile(0, 6), Tile(1, 2),
        Tile(1, 3), Tile(1, 4), Tile(1, 5), Tile(1, 6), Tile(2, 3), Tile(2, 4), Tile(2, 5),
        Tile(6, 6), Tile(2, 6), Tile(3, 4), Tile(3, 5), Tile(3, 6), Tile(4, 5), Tile(4, 6),
        Tile(5, 6), Tile(0, 0), Tile(1, 1), Tile(2, 2), Tile(3, 3), Tile(4, 4), Tile(5, 5),
    )

    /** Seat 1 opens with 6|6; seat 2 already holds 6|0. */
    private val placementAvailableDeck = listOf(
        Tile(6, 6), Tile(6, 5), Tile(5, 4), Tile(4, 3), Tile(3, 2), Tile(2, 1), Tile(1, 0),
        Tile(6, 0), Tile(0, 0), Tile(5, 5), Tile(4, 4), Tile(3, 3), Tile(2, 2), Tile(1, 1),
        Tile(0, 2), Tile(0, 3), Tile(0, 4), Tile(0, 5), Tile(1, 3), Tile(1, 4), Tile(1, 5),
        Tile(1, 6), Tile(2, 4), Tile(2, 5), Tile(2, 6), Tile(3, 5), Tile(3, 6), Tile(4, 6),
    )

    private fun snapshot(match: Match): List<Any> = listOf(
        match.board.placedTiles,
        match.seats.map { it.hand },
        match.boneyard,
        match.activeSeatIndex,
        match.consecutivePasses,
        match.status,
    )

    private fun allTiles(match: Match): List<Tile> =
        match.board.placedTiles.map { it.tile } + match.seats.flatMap { it.hand } + match.boneyard

    private fun assertRejected(reason: RejectionReason, block: () -> Unit) {
        val exception = assertThrows(MoveRejectedException::class.java) { block() }
        assertEquals(reason, exception.reason)
    }

    @Test
    fun `drawing stops at the first playable tile and keeps the turn`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, drawDeck)

        val drawn = match.drawUntilPlayable(1)

        assertEquals(listOf(Tile(0, 3), Tile(0, 4), Tile(0, 6)), drawn)
        assertEquals(1, match.activeSeatIndex)
        assertTrue(match.seats[1].hand.containsAll(drawn))
        assertTrue(match.boneyard.contains(Tile(0, 5)))
    }

    @Test
    fun `drawing all remaining tiles leaves the boneyard empty`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, blockedDeck)

        val drawn = match.drawUntilPlayable(1)

        assertEquals(14, drawn.size)
        assertTrue(match.boneyard.isEmpty())
        assertEquals(21, match.seats[1].hand.size)
        assertEquals(1, match.activeSeatIndex)
    }

    @Test
    fun `drawing is rejected when the player has a legal placement`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, placementAvailableDeck)
        val before = snapshot(match)

        assertRejected(RejectionReason.PLACEMENT_AVAILABLE) { match.drawUntilPlayable(1) }

        assertEquals(before, snapshot(match))
    }

    @Test
    fun `drawing is rejected when the boneyard is empty`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipNoSixDeck)
        val before = snapshot(match)

        assertRejected(RejectionReason.NOTHING_TO_DRAW) { match.drawUntilPlayable(3) }

        assertEquals(before, snapshot(match))
    }

    @Test
    fun `drawing out of turn is rejected`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, drawDeck)
        val before = snapshot(match)

        assertRejected(RejectionReason.NOT_YOUR_TURN) { match.drawUntilPlayable(0) }

        assertEquals(before, snapshot(match))
    }

    @Test
    fun `passing is rejected while tiles remain to draw`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, blockedDeck)
        val before = snapshot(match)

        assertEquals(
            MoveOutcome.Rejected(RejectionReason.MUST_DRAW),
            match.recordPass(1),
        )
        assertEquals(before, snapshot(match))
    }

    @Test
    fun `passing is rejected when the player has a legal placement`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipPlayableDeck)
        val before = snapshot(match)

        assertEquals(
            MoveOutcome.Rejected(RejectionReason.PLACEMENT_AVAILABLE),
            match.recordPass(3),
        )
        assertEquals(before, snapshot(match))
    }

    @Test
    fun `passing out of turn is rejected`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, blockedDeck)
        val before = snapshot(match)

        assertEquals(
            MoveOutcome.Rejected(RejectionReason.NOT_YOUR_TURN),
            match.recordPass(0),
        )
        assertEquals(before, snapshot(match))
    }

    @Test
    fun `an accepted pass advances the turn and counts`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, blockedDeck)
        match.drawUntilPlayable(1)

        assertEquals(MoveOutcome.Accepted, match.recordPass(1))

        assertEquals(1, match.consecutivePasses)
        assertEquals(0, match.activeSeatIndex)
        assertEquals(MatchStatus.ACTIVE, match.status)
    }

    @Test
    fun `the last seat's pass wraps to seat one`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipNoSixDeck)

        assertEquals(MoveOutcome.Accepted, match.recordPass(3))

        assertTrue(match.boneyard.isEmpty())
        assertEquals(1, match.consecutivePasses)
        assertEquals(0, match.activeSeatIndex)
    }

    @Test
    fun `a placement resets consecutive passes after a pass`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipNoSixDeck)
        assertEquals(MoveOutcome.Accepted, match.recordPass(3))
        assertEquals(1, match.consecutivePasses)

        assertEquals(
            MoveOutcome.Accepted,
            match.applyPlacement(seatIndex = 0, tile = Tile(0, 6), end = BoardEnd.LEFT),
        )

        assertEquals(0, match.consecutivePasses)
    }

    @Test
    fun `three-player passes advance in seat order`() {
        val match = Match.start(MatchConfiguration.THREE_PLAYER_INDIVIDUAL, threePlayerDeck)
        match.drawUntilPlayable(1)

        assertEquals(MoveOutcome.Accepted, match.recordPass(1))
        assertEquals(1, match.consecutivePasses)
        assertEquals(2, match.activeSeatIndex)

        assertEquals(MoveOutcome.Accepted, match.recordPass(2))
        assertEquals(2, match.consecutivePasses)
        assertEquals(0, match.activeSeatIndex)
    }

    @Test
    fun `tiles are conserved across board, hands and boneyard`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, drawDeck)
        match.drawUntilPlayable(1)
        match.applyPlacement(seatIndex = 1, tile = Tile(0, 6), end = BoardEnd.RIGHT)

        val tiles = allTiles(match)

        assertEquals(28, tiles.size)
        assertEquals(DominoSet.standardDoubleSix().toSet(), tiles.toSet())
    }
}
