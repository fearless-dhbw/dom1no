package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Blocked results are reached by replaying complete, legal games: every placement, draw and pass
 * below is an accepted action, so the blocked state is naturally reachable rather than seeded.
 */
class MatchBlockedResultTest {

    private fun assertBlocked(match: Match) {
        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(EndReason.BLOCKED, match.result?.endReason)
        assertEquals(match.seats.size, match.consecutivePasses)
        assertTrue(match.boneyard.isEmpty())
    }

    /** Ends 5|5 with hands worth 22 and 0 pips. */
    private val twoPlayerWinnerDeck = deckOf(
        "36 55 13 05 25 46 03 35 06 14 02 23 00 34 " +
            "01 24 04 11 15 16 44 22 33 66 12 56 26 45",
    )

    private val twoPlayerWinnerScript = """
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

    /** Ends 5|5 with both hands worth 19 pips. */
    private val twoPlayerTieDeck = deckOf(
        "26 02 25 56 34 11 33 04 66 00 44 13 36 23 " +
            "46 03 12 06 22 15 01 05 45 16 55 35 14 24",
    )

    private val twoPlayerTieScript = """
        p0 56 L
        p1 36 R
        p0 33 R
        p1 13 R
        p0 25 L
        p1 23 L
        p0 11 R
        d1 46,03
        p1 03 L
        p0 02 L
        d1 12
        p1 12 L
        d0 06,22,15
        p0 15 L
        d1 01
        p1 01 R
        p0 06 R
        p1 46 R
        p0 34 R
        d1 05
        p1 05 L
        d0 45,16,55,35
        p0 35 R
        p1 00 L
        p0 55 R
        p1 04 L
        p0 45 L
        d1 14,24
        x1
        x0
    """

    /** Ends 5|5 with hands worth 4, 54 and 4 pips. */
    private val threePlayerTieDeck = deckOf(
        "13 00 45 35 56 24 16 36 06 04 23 05 15 26 03 12 14 25 44 55 22 02 46 01 34 66 11 33",
    )

    private val threePlayerTieScript = """
        p0 45 L
        p1 05 R
        p2 44 L
        p0 00 R
        p1 06 R
        p2 14 L
        p0 16 L
        p1 26 L
        p2 12 L
        p0 56 R
        p1 15 L
        p2 25 L
        p0 24 L
        p1 04 L
        p2 03 L
        p0 35 L
        d1 02,46,01,34,66,11,33
        x1
        x2
        x0
    """

    /** Ends 5|5 with team totals of 40 and 44 pips. */
    private val partnershipWinnerDeck = deckOf(
        "06 55 13 11 56 45 24 16 33 36 25 44 02 15 " +
            "01 03 66 34 22 26 23 05 12 35 04 46 14 00",
    )

    private val partnershipWinnerScript = """
        p3 46 L
        p0 56 R
        p1 15 R
        p2 01 R
        p3 05 R
        p0 45 L
        p1 25 L
        p2 23 L
        p3 35 L
        p0 55 L
        x1
        x2
        x3
        x0
    """

    /** Ends 1|1 with both team totals at 25 pips. */
    private val partnershipTieDeck = deckOf(
        "15 02 25 23 12 36 05 01 11 04 56 14 45 16 " +
            "03 44 55 13 26 24 00 66 34 22 33 46 06 35",
    )

    private val partnershipTieScript = """
        p0 36 L
        p1 56 R
        p2 55 R
        p3 35 L
        p0 15 L
        p1 14 L
        p2 24 L
        p3 22 L
        p0 12 L
        p1 11 L
        p2 13 L
        p3 34 L
        p0 05 R
        p1 01 R
        p2 44 L
        p3 46 L
        x0
        p1 16 L
        x2
        x3
        x0
        x1
    """

    @Test
    fun `a blocked two-player match is won by the lower pip hand`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, twoPlayerWinnerDeck)

        replay(match, twoPlayerWinnerScript)

        assertBlocked(match)
        assertEquals(mapOf(1 to 22, 2 to 0), match.result?.remainingPips)
        assertEquals(
            mapOf(1 to ParticipantOutcome.LOSS, 2 to ParticipantOutcome.WIN),
            match.result?.outcomes,
        )
        assertEquals(setOf(2), match.result?.winners)
        assertFalse(match.result?.isDraw == true)
    }

    @Test
    fun `a blocked two-player match with equal pips is a draw`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, twoPlayerTieDeck)

        replay(match, twoPlayerTieScript)

        assertBlocked(match)
        assertEquals(mapOf(1 to 19, 2 to 19), match.result?.remainingPips)
        assertEquals(
            mapOf(1 to ParticipantOutcome.DRAW, 2 to ParticipantOutcome.DRAW),
            match.result?.outcomes,
        )
        assertTrue(match.result?.isDraw == true)
    }

    @Test
    fun `a blocked three-player match draws the tied lowest hands`() {
        val match = Match.start(MatchConfiguration.THREE_PLAYER_INDIVIDUAL, threePlayerTieDeck)

        replay(match, threePlayerTieScript)

        assertBlocked(match)
        assertEquals(mapOf(1 to 4, 2 to 54, 3 to 4), match.result?.remainingPips)
        assertEquals(
            mapOf(
                1 to ParticipantOutcome.DRAW,
                2 to ParticipantOutcome.LOSS,
                3 to ParticipantOutcome.DRAW,
            ),
            match.result?.outcomes,
        )
        assertTrue(match.result?.winners?.isEmpty() == true)
    }

    @Test
    fun `a blocked partnership match compares combined team pips`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipWinnerDeck)

        replay(match, partnershipWinnerScript)

        assertBlocked(match)
        assertEquals(mapOf(1 to 18, 2 to 32, 3 to 22, 4 to 12), match.result?.remainingPips)
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
    fun `a blocked partnership match with equal team pips is a draw`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipTieDeck)

        replay(match, partnershipTieScript)

        assertBlocked(match)
        assertEquals(mapOf(1 to 14, 2 to 13, 3 to 11, 4 to 12), match.result?.remainingPips)
        assertEquals(
            listOf(1, 2, 3, 4).associateWith { ParticipantOutcome.DRAW },
            match.result?.outcomes,
        )
        assertTrue(match.result?.isDraw == true)
    }

    @Test
    fun `a blocked match rejects every further action`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipWinnerDeck)
        replay(match, partnershipWinnerScript)
        val result = match.result
        val active = match.activeSeatIndex

        assertEquals(
            MoveOutcome.Rejected(RejectionReason.MATCH_NOT_ACTIVE),
            match.applyPlacement(active, match.activeSeat.hand.first(), BoardEnd.LEFT),
        )
        assertThrows(MoveRejectedException::class.java) { match.drawUntilPlayable(active) }
        assertEquals(
            MoveOutcome.Rejected(RejectionReason.MATCH_NOT_ACTIVE),
            match.recordPass(active),
        )
        assertEquals(result, match.result)
    }
}
