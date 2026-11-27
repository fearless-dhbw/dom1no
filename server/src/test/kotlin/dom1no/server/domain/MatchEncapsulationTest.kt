package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.random.Random

/**
 * The authoritative match must only change through its validated actions. These tests guard the
 * boundary between [Match] and callers that can read its board, seats and result.
 */
class MatchEncapsulationTest {

    private fun startedMatch() =
        Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, Random(7))

    private fun snapshot(match: Match): List<Any> = listOf(
        match.board.placedTiles,
        match.seats.map { it.hand },
        match.boneyard,
        match.activeSeatIndex,
        match.consecutivePasses,
        match.status,
    )

    @Test
    fun `the exposed board offers no placement mutator`() {
        val match = startedMatch()

        assertThrows(NoSuchMethodException::class.java) {
            match.board.javaClass.getMethod("place", Tile::class.java, BoardEnd::class.java)
        }
    }

    @Test
    fun `the exposed seats offer no hand mutators`() {
        val match = startedMatch()

        assertThrows(NoSuchMethodException::class.java) {
            match.seats[0].javaClass.getMethod("addTile", Tile::class.java)
        }
        assertThrows(NoSuchMethodException::class.java) {
            match.seats[0].javaClass.getMethod("removeTile", Tile::class.java)
        }
    }

    @Test
    fun `clearing the exposed seat list does not change the match`() {
        val match = startedMatch()
        val before = snapshot(match)

        runCatching {
            @Suppress("UNCHECKED_CAST")
            (match.seats as MutableList<Any>).clear()
        }

        assertEquals(before, snapshot(match))
    }

    @Test
    fun `changing an exposed hand snapshot does not change the match`() {
        val match = startedMatch()
        val before = snapshot(match)

        runCatching {
            @Suppress("UNCHECKED_CAST")
            (match.seats[0].hand as MutableList<Tile>).clear()
        }

        assertEquals(before, snapshot(match))
    }

    @Test
    fun `changing an exposed board snapshot does not change the match`() {
        val match = startedMatch()
        val before = snapshot(match)

        runCatching {
            @Suppress("UNCHECKED_CAST")
            (match.board.placedTiles as MutableList<PlacedTile>).clear()
        }

        assertEquals(before, snapshot(match))
    }

    @Test
    fun `a finished result survives attempts to change exposed state`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, twoPlayerEmptyHandDeck)
        replay(match, twoPlayerEmptyHandScript)
        val result = match.result
        val before = snapshot(match)

        runCatching {
            @Suppress("UNCHECKED_CAST")
            (match.seats[0].hand as MutableList<Tile>).add(Tile(0, 0))
        }
        runCatching {
            @Suppress("UNCHECKED_CAST")
            (match.board.placedTiles as MutableList<PlacedTile>).clear()
        }

        assertEquals(before, snapshot(match))
        assertEquals(result, match.result)
        assertEquals(MatchStatus.FINISHED, match.status)
    }

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
}
