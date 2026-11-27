package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class MatchSetupTest {

    /** Puts the supplied tiles first, so they are dealt before the rest of the set. */
    private fun deckWith(vararg ordered: Tile): List<Tile> {
        val first = ordered.toList()
        return first + DominoSet.standardDoubleSix().filterNot { it in first }
    }

    private fun everyTile(match: Match): List<Tile> =
        match.board.placedTiles.map { it.tile } + match.seats.flatMap { it.hand } + match.boneyard

    private fun handOf(match: Match, seatNumber: Int): List<Tile> =
        match.seats.single { it.seatNumber == seatNumber }.hand

    /** Every seat is dealt seven tiles; the opener then plays one, leaving six. */
    private fun assertDealtThenOpened(match: Match) {
        assertTrue(match.seats.all { it.hand.size == if (it === match.opener) 6 else 7 })
    }

    /** Seat 1 holds 4|4, seat 2 holds the higher 5|5, and 6|6 stays in the boneyard. */
    private val highestDoubleDeck = deckWith(
        Tile(4, 4), Tile(0, 1), Tile(0, 2), Tile(0, 3), Tile(0, 5), Tile(1, 2), Tile(1, 3),
        Tile(5, 5), Tile(0, 4), Tile(0, 6), Tile(1, 4), Tile(1, 5), Tile(2, 3), Tile(2, 4),
    )

    /** No doubles are dealt; 5|4 and 6|3 both total nine, and the higher end of 6|3 wins. */
    private val pipTotalTieDeck = deckWith(
        Tile(5, 4), Tile(0, 1), Tile(0, 2), Tile(0, 3), Tile(0, 4), Tile(0, 5), Tile(1, 2),
        Tile(6, 3), Tile(1, 3), Tile(1, 4), Tile(1, 5), Tile(2, 3), Tile(2, 4), Tile(2, 5),
    )

    /** Every double stays in the boneyard, so the highest non-double 6|5 opens. */
    private val doublesInBoneyardDeck = deckWith(
        Tile(6, 5), Tile(0, 1), Tile(0, 2), Tile(0, 3), Tile(0, 4), Tile(0, 6), Tile(1, 2),
        Tile(1, 3), Tile(1, 4), Tile(1, 5), Tile(1, 6), Tile(2, 3), Tile(2, 4), Tile(2, 5),
    )

    /** Seat 3 holds 6|6, which must open a partnership match. */
    private val partnershipDeck = deckWith(
        Tile(0, 1), Tile(0, 2), Tile(0, 3), Tile(0, 4), Tile(0, 5), Tile(0, 6), Tile(1, 2),
        Tile(1, 3), Tile(1, 4), Tile(1, 5), Tile(1, 6), Tile(2, 3), Tile(2, 4), Tile(2, 5),
        Tile(6, 6), Tile(2, 6), Tile(3, 4), Tile(3, 5), Tile(3, 6), Tile(4, 5), Tile(4, 6),
        Tile(5, 6), Tile(0, 0), Tile(1, 1), Tile(2, 2), Tile(3, 3), Tile(4, 4), Tile(5, 5),
    )

    @Test
    fun `two-player match deals seven tiles to each seat`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, highestDoubleDeck)

        assertEquals(2, match.seats.size)
        assertDealtThenOpened(match)
    }

    @Test
    fun `two-player match keeps fourteen tiles in the boneyard`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, highestDoubleDeck)

        assertEquals(14, match.boneyard.size)
    }

    @Test
    fun `three-player match keeps seven tiles in the boneyard`() {
        val match = Match.start(MatchConfiguration.THREE_PLAYER_INDIVIDUAL, highestDoubleDeck)

        assertEquals(3, match.seats.size)
        assertDealtThenOpened(match)
        assertEquals(7, match.boneyard.size)
    }

    @Test
    fun `four-player match deals every tile and has no boneyard`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipDeck)

        assertEquals(4, match.seats.size)
        assertDealtThenOpened(match)
        assertTrue(match.boneyard.isEmpty())
    }

    @Test
    fun `every tile is on the board, in a hand or in the boneyard exactly once`() {
        val configurations = listOf(
            MatchConfiguration.TWO_PLAYER_INDIVIDUAL to highestDoubleDeck,
            MatchConfiguration.THREE_PLAYER_INDIVIDUAL to highestDoubleDeck,
            MatchConfiguration.FOUR_PLAYER_PARTNERSHIP to partnershipDeck,
        )

        for ((configuration, deck) in configurations) {
            val tiles = everyTile(Match.start(configuration, deck))

            assertEquals(28, tiles.size, "tile count for $configuration")
            assertEquals(28, tiles.toSet().size, "duplicates for $configuration")
            assertEquals(DominoSet.standardDoubleSix().toSet(), tiles.toSet(), "tiles for $configuration")
        }
    }

    @Test
    fun `the highest dealt double opens an individual match`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, highestDoubleDeck)

        assertEquals(Tile(5, 5), match.openingTile)
        assertEquals(2, match.opener.seatNumber)
    }

    @Test
    fun `the higher end breaks a pip-total tie when no double is dealt`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, pipTotalTieDeck)

        assertEquals(Tile(3, 6), match.openingTile)
    }

    @Test
    fun `a double in the boneyard cannot open the match`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, doublesInBoneyardDeck)

        assertEquals(Tile(5, 6), match.openingTile)
        assertTrue(match.boneyard.contains(Tile(6, 6)))
    }

    @Test
    fun `the holder of six-six opens a partnership match`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipDeck)

        assertEquals(Tile(6, 6), match.openingTile)
        assertEquals(3, match.opener.seatNumber)
    }

    @Test
    fun `the opening tile leaves the opener's hand and starts the board`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, highestDoubleDeck)

        assertEquals(6, handOf(match, 2).size)
        assertEquals(1, match.board.placedTiles.size)
        assertEquals(Tile(5, 5), match.board.placedTiles.single().tile)
    }

    @Test
    fun `the next seat becomes active after the opening placement`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, doublesInBoneyardDeck)

        assertEquals(1, match.opener.seatNumber)
        assertEquals(2, match.activeSeat.seatNumber)
    }

    @Test
    fun `the active seat wraps to seat one when the last seat opens`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, highestDoubleDeck)

        assertEquals(2, match.opener.seatNumber)
        assertEquals(1, match.activeSeat.seatNumber)
    }

    @Test
    fun `a new match is active with no consecutive passes`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, highestDoubleDeck)

        assertEquals(MatchStatus.ACTIVE, match.status)
        assertEquals(0, match.consecutivePasses)
    }

    @Test
    fun `opposite partnership seats share a team`() {
        val match = Match.start(MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, partnershipDeck)

        assertEquals(1, match.seats.single { it.seatNumber == 1 }.teamNumber)
        assertEquals(2, match.seats.single { it.seatNumber == 2 }.teamNumber)
        assertEquals(1, match.seats.single { it.seatNumber == 3 }.teamNumber)
        assertEquals(2, match.seats.single { it.seatNumber == 4 }.teamNumber)
    }

    @Test
    fun `individual seats have no team`() {
        val match = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, highestDoubleDeck)

        assertTrue(match.seats.all { it.teamNumber == null })
    }

    @Test
    fun `the same seed deals the same tiles`() {
        val first = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, Random(42))
        val second = Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, Random(42))

        assertEquals(first.seats.map { it.hand }, second.seats.map { it.hand })
        assertEquals(first.boneyard, second.boneyard)
        assertEquals(first.openingTile, second.openingTile)
    }

    @Test
    fun `a deck that is not the double-six set is rejected`() {
        val incomplete = DominoSet.standardDoubleSix().drop(1)

        assertThrows(IllegalArgumentException::class.java) {
            Match.start(MatchConfiguration.TWO_PLAYER_INDIVIDUAL, incomplete)
        }
    }
}
