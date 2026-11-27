package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchPlacementTest {

    /** Puts the supplied tiles first, so they are dealt before the rest of the set. */
    private fun deckWith(vararg ordered: Tile): List<Tile> {
        val first = ordered.toList()
        return first + DominoSet.standardDoubleSix().filterNot { it in first }
    }

    /**
     * Seat 1 opens with 6|6 and holds a descending chain. Seat 2 holds one matching tile for each
     * step, so the two seats can play the match out in a fixed order.
     */
    private val twoPlayerChainDeck = deckWith(
        Tile(6, 6), Tile(6, 5), Tile(5, 4), Tile(4, 3), Tile(3, 2), Tile(2, 1), Tile(1, 0),
        Tile(6, 0), Tile(0, 0), Tile(5, 5), Tile(4, 4), Tile(3, 3), Tile(2, 2), Tile(1, 1),
    )

    /** Seat 3 opens with 6|6; each seat has one tile matching the end left by the previous seat. */
    private val threePlayerDeck = deckWith(
        Tile(6, 1), Tile(1, 1), Tile(0, 0), Tile(2, 2), Tile(3, 3), Tile(4, 4), Tile(5, 5),
        Tile(6, 2), Tile(2, 1), Tile(0, 1), Tile(0, 2), Tile(0, 3), Tile(0, 4), Tile(0, 5),
        Tile(6, 6), Tile(6, 3), Tile(3, 1), Tile(3, 4), Tile(4, 5), Tile(5, 6), Tile(3, 5),
    )

    /** Seat 3 opens with 6|6 and seat 4 holds 5|6, so seat 4 can place and wrap to seat 1. */
    private val partnershipDeck = deckWith(
        Tile(0, 1), Tile(0, 2), Tile(0, 3), Tile(0, 4), Tile(0, 5), Tile(0, 6), Tile(1, 2),
        Tile(1, 3), Tile(1, 4), Tile(1, 5), Tile(1, 6), Tile(2, 3), Tile(2, 4), Tile(2, 5),
        Tile(6, 6), Tile(2, 6), Tile(3, 4), Tile(3, 5), Tile(3, 6), Tile(4, 5), Tile(4, 6),
        Tile(5, 6), Tile(0, 0), Tile(1, 1), Tile(2, 2), Tile(3, 3), Tile(4, 4), Tile(5, 5),
    )

    private fun startedMatch() =
        Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, twoPlayerChainDeck)

    private fun snapshot(match: Match): List<Any> = listOf(
        match.board.placedTiles,
        match.seats.map { it.hand },
        match.activeSeatIndex,
        match.consecutivePasses,
        match.status,
    )

    /** Plays every remaining tile in a fixed order until seat 1 empties its hand. */
    private fun playOutTwoPlayerMatch(match: Match) {
        val placements = listOf(
            1 to (Tile(6, 0) to BoardEnd.RIGHT),
            0 to (Tile(6, 5) to BoardEnd.LEFT),
            1 to (Tile(0, 0) to BoardEnd.RIGHT),
            0 to (Tile(5, 4) to BoardEnd.LEFT),
            1 to (Tile(4, 4) to BoardEnd.LEFT),
            0 to (Tile(4, 3) to BoardEnd.LEFT),
            1 to (Tile(3, 3) to BoardEnd.LEFT),
            0 to (Tile(3, 2) to BoardEnd.LEFT),
            1 to (Tile(2, 2) to BoardEnd.LEFT),
            0 to (Tile(2, 1) to BoardEnd.LEFT),
            1 to (Tile(1, 1) to BoardEnd.LEFT),
            0 to (Tile(1, 0) to BoardEnd.LEFT),
        )

        for ((seatIndex, placement) in placements) {
            val (tile, end) = placement
            assertEquals(MoveOutcome.Accepted, match.applyPlacement(seatIndex, tile, end))
        }
    }

    @Test
    fun `a legal placement removes the tile, extends the board and advances the turn`() {
        val match = startedMatch()

        val outcome = match.applyPlacement(seatIndex = 1, tile = Tile(6, 0), end = BoardEnd.RIGHT)

        assertEquals(MoveOutcome.Accepted, outcome)
        assertFalse(match.seats[1].hasTile(Tile(6, 0)))
        assertEquals(6, match.seats[1].hand.size)
        assertEquals(PlacedTile(6, 0), match.board.placedTiles.last())
        assertEquals(6, match.board.openLeft)
        assertEquals(0, match.board.openRight)
        assertEquals(0, match.activeSeatIndex)
    }

    @Test
    fun `a left placement exposes the other pip at the left end`() {
        val match = startedMatch()

        assertEquals(
            MoveOutcome.Accepted,
            match.applyPlacement(seatIndex = 1, tile = Tile(6, 0), end = BoardEnd.LEFT),
        )

        assertEquals(PlacedTile(0, 6), match.board.placedTiles.first())
        assertEquals(0, match.board.openLeft)
        assertEquals(6, match.board.openRight)
    }

    @Test
    fun `a placement out of turn is rejected without changing the match`() {
        val match = startedMatch()
        val before = snapshot(match)

        val outcome = match.applyPlacement(seatIndex = 0, tile = Tile(6, 5), end = BoardEnd.LEFT)

        assertEquals(MoveOutcome.Rejected(RejectionReason.NOT_YOUR_TURN), outcome)
        assertEquals(before, snapshot(match))
    }

    @Test
    fun `a tile outside the acting hand is rejected without changing the match`() {
        val match = startedMatch()
        val before = snapshot(match)

        val outcome = match.applyPlacement(seatIndex = 1, tile = Tile(6, 5), end = BoardEnd.LEFT)

        assertEquals(MoveOutcome.Rejected(RejectionReason.TILE_NOT_IN_HAND), outcome)
        assertEquals(before, snapshot(match))
    }

    @Test
    fun `a tile that matches no open end is rejected without changing the match`() {
        val match = startedMatch()
        val before = snapshot(match)

        val outcome = match.applyPlacement(seatIndex = 1, tile = Tile(5, 5), end = BoardEnd.LEFT)

        assertEquals(MoveOutcome.Rejected(RejectionReason.ILLEGAL_PLACEMENT), outcome)
        assertEquals(before, snapshot(match))
    }

    @Test
    fun `the turn advances through three seats and wraps to seat one`() {
        val match = Match.start(MatchConfiguration.THREE_PLAYER_INDIVIDUAL, threePlayerDeck)

        assertEquals(0, match.activeSeatIndex)
        assertEquals(
            MoveOutcome.Accepted,
            match.applyPlacement(seatIndex = 0, tile = Tile(6, 1), end = BoardEnd.RIGHT),
        )
        assertEquals(1, match.activeSeatIndex)

        assertEquals(
            MoveOutcome.Accepted,
            match.applyPlacement(seatIndex = 1, tile = Tile(6, 2), end = BoardEnd.LEFT),
        )
        assertEquals(2, match.activeSeatIndex)

        assertEquals(
            MoveOutcome.Accepted,
            match.applyPlacement(seatIndex = 2, tile = Tile(3, 1), end = BoardEnd.RIGHT),
        )
        assertEquals(0, match.activeSeatIndex)
    }

    @Test
    fun `a four-player placement wraps from the last seat to seat one`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipDeck)

        assertEquals(3, match.activeSeatIndex)
        assertEquals(
            MoveOutcome.Accepted,
            match.applyPlacement(seatIndex = 3, tile = Tile(5, 6), end = BoardEnd.LEFT),
        )

        assertEquals(0, match.activeSeatIndex)
        assertEquals(6, match.seats[3].hand.size)
    }

    @Test
    fun `an empty hand ends the match and does not advance the turn`() {
        val match = startedMatch()

        playOutTwoPlayerMatch(match)

        assertEquals(MatchStatus.FINISHED, match.status)
        assertTrue(match.seats[0].hand.isEmpty())
        assertEquals(0, match.activeSeatIndex)
        assertEquals(listOf(Tile(5, 5)), match.seats[1].hand)
        assertEquals(13, match.board.placedTiles.size)
    }

    @Test
    fun `a finished match rejects further placements`() {
        val match = startedMatch()
        playOutTwoPlayerMatch(match)

        val outcome = match.applyPlacement(seatIndex = 0, tile = Tile(5, 5), end = BoardEnd.LEFT)

        assertEquals(MoveOutcome.Rejected(RejectionReason.MATCH_NOT_ACTIVE), outcome)
        assertEquals(MatchStatus.FINISHED, match.status)
        assertTrue(match.seats[0].hand.isEmpty())
    }
}
