package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SeatTest {

    @Test
    fun `seat reports whether it holds a tile`() {
        val seat = Seat(1, null, listOf(Tile(3, 5), Tile(6, 6)))

        assertTrue(seat.hasTile(Tile(5, 3)))
        assertFalse(seat.hasTile(Tile(3, 6)))
    }

    @Test
    fun `removing a held tile updates the hand`() {
        val seat = Seat(1, null, listOf(Tile(3, 5), Tile(6, 6)))

        seat.removeTile(Tile(5, 3))

        assertEquals(listOf(Tile(6, 6)), seat.hand)
    }

    @Test
    fun `removing a tile that is not held is rejected`() {
        val seat = Seat(1, null, listOf(Tile(3, 5)))

        assertThrows(IllegalArgumentException::class.java) {
            seat.removeTile(Tile(3, 6))
        }
        assertEquals(listOf(Tile(3, 5)), seat.hand)
    }

    @Test
    fun `remaining pips sums the pips of the hand`() {
        val seat = Seat(1, null, listOf(Tile(6, 6), Tile(0, 0), Tile(1, 1)))

        assertEquals(14, seat.remainingPips())
    }
}
