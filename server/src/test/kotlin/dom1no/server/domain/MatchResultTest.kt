package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchResultTest {

    /** Seat 1 opens with 6|6 and plays its remaining six tiles without drawing. */
    private val twoPlayerEmptyHandDeck = deckOf(
        "66 65 54 43 32 21 10 60 00 55 44 33 22 11 " +
            "02 03 04 05 13 14 15 16 24 25 26 35 36 46",
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

    /** Seat 3 empties its hand after a mix of placements, draws and passes. */
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

    @Test
    fun `an active match has no result`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, twoPlayerEmptyHandDeck)

        assertNull(match.result)
        assertThrows(IllegalStateException::class.java) { match.evaluateOutcome() }
    }

    @Test
    fun `an individual empty hand wins immediately`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, twoPlayerEmptyHandDeck)

        replay(match, twoPlayerEmptyHandScript)

        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(EndReason.EMPTY_HAND, match.result?.endReason)
        assertEquals(
            mapOf(1 to ParticipantOutcome.WIN, 2 to ParticipantOutcome.LOSS),
            match.result?.outcomes,
        )
        assertEquals(setOf(1), match.result?.winners)
        assertTrue(match.result?.remainingPips?.isEmpty() == true)
    }

    @Test
    fun `a three-player empty hand wins for the seat that emptied`() {
        val match = Match.start(MatchConfiguration.THREE_PLAYER_INDIVIDUAL, threePlayerEmptyHandDeck)

        replay(match, threePlayerEmptyHandScript)

        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(EndReason.EMPTY_HAND, match.result?.endReason)
        assertEquals(
            mapOf(
                1 to ParticipantOutcome.LOSS,
                2 to ParticipantOutcome.LOSS,
                3 to ParticipantOutcome.WIN,
            ),
            match.result?.outcomes,
        )
        assertEquals(setOf(3), match.result?.winners)
    }

    @Test
    fun `a partnership empty hand wins for the whole team`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipEmptyHandDeck)

        replay(match, partnershipEmptyHandScript)

        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(EndReason.EMPTY_HAND, match.result?.endReason)
        assertEquals(
            mapOf(
                1 to ParticipantOutcome.WIN,
                2 to ParticipantOutcome.LOSS,
                3 to ParticipantOutcome.WIN,
                4 to ParticipantOutcome.LOSS,
            ),
            match.result?.outcomes,
        )
        assertEquals(setOf(1, 3), match.result?.winners)
    }

    @Test
    fun `a finished match rejects every further action`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, twoPlayerEmptyHandDeck)
        replay(match, twoPlayerEmptyHandScript)
        val result = match.result

        assertEquals(
            MoveOutcome.Rejected(RejectionReason.MATCH_NOT_ACTIVE),
            match.applyPlacement(seatIndex = 1, tile = Tile(5, 5), end = BoardEnd.LEFT),
        )
        assertThrows(MoveRejectedException::class.java) { match.drawUntilPlayable(0) }
        assertEquals(
            MoveOutcome.Rejected(RejectionReason.MATCH_NOT_ACTIVE),
            match.recordPass(1),
        )
        assertEquals(result, match.result)
    }
}
