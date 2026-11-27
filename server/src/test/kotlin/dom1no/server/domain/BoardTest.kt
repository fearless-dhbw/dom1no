package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BoardTest {

    /** A board whose open ends are [left] and [right], e.g. the 3 and 1 ends from the rules example. */
    private fun boardWithEnds(left: Int, right: Int) = Board().apply {
        place(Tile(left, right), BoardEnd.LEFT)
    }

    @Test
    fun `empty board has no open ends and no tiles`() {
        val board = Board()

        assertNull(board.openLeft)
        assertNull(board.openRight)
        assertTrue(board.placedTiles.isEmpty())
    }

    @Test
    fun `first placement sets both open ends`() {
        val board = Board()

        board.place(Tile(6, 3), BoardEnd.LEFT)

        assertEquals(6, board.openLeft)
        assertEquals(3, board.openRight)
        assertEquals(listOf(Tile(6, 3)), board.placedTiles)
    }

    @Test
    fun `placement at the left end exposes the other pip`() {
        val board = boardWithEnds(left = 3, right = 1)

        board.place(Tile(3, 5), BoardEnd.LEFT)

        assertEquals(5, board.openLeft)
        assertEquals(1, board.openRight)
        assertEquals(listOf(Tile(5, 3), Tile(3, 1)), board.placedTiles)
    }

    @Test
    fun `placement at the right end exposes the other pip`() {
        val board = boardWithEnds(left = 3, right = 1)

        board.place(Tile(4, 1), BoardEnd.RIGHT)

        assertEquals(3, board.openLeft)
        assertEquals(4, board.openRight)
        assertEquals(listOf(Tile(3, 1), Tile(1, 4)), board.placedTiles)
    }

    @Test
    fun `tile already oriented towards the end keeps its orientation`() {
        val board = boardWithEnds(left = 3, right = 1)

        board.place(Tile(5, 3), BoardEnd.LEFT)

        assertEquals(Tile(5, 3), board.placedTiles.first())
        assertEquals(5, board.openLeft)
    }

    @Test
    fun `tile that matches neither open end cannot be placed`() {
        val board = boardWithEnds(left = 3, right = 1)

        assertFalse(board.canPlace(Tile(6, 6), BoardEnd.LEFT))
        assertFalse(board.canPlace(Tile(6, 6), BoardEnd.RIGHT))
    }

    @Test
    fun `tile that matches an open end can be placed`() {
        val board = boardWithEnds(left = 3, right = 1)

        assertTrue(board.canPlace(Tile(3, 5), BoardEnd.LEFT))
        assertTrue(board.canPlace(Tile(4, 1), BoardEnd.RIGHT))
    }

    @Test
    fun `rejected placement leaves board state unchanged`() {
        val board = boardWithEnds(left = 3, right = 1)
        val endsBefore = board.openLeft to board.openRight
        val tilesBefore = board.placedTiles

        assertThrows(IllegalPlacementException::class.java) {
            board.place(Tile(6, 6), BoardEnd.LEFT)
        }

        assertEquals(endsBefore, board.openLeft to board.openRight)
        assertEquals(tilesBefore, board.placedTiles)
    }

    @Test
    fun `rejected placement explains why it was rejected`() {
        val board = boardWithEnds(left = 3, right = 1)

        val error = assertThrows(IllegalPlacementException::class.java) {
            board.place(Tile(6, 6), BoardEnd.LEFT)
        }

        assertTrue(error.message!!.contains("6|6"))
        assertTrue(error.message!!.contains("3"))
        assertTrue(error.message!!.contains("1"))
    }
}
