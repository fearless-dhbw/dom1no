package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TileTest {

    @Test
    fun `tile rejects pips outside the double-six range`() {
        assertThrows(IllegalArgumentException::class.java) { Tile(-1, 3) }
        assertThrows(IllegalArgumentException::class.java) { Tile(3, 7) }
    }

    @Test
    fun `tile matches either of its pips`() {
        val tile = Tile(3, 5)

        assertTrue(tile.matches(3))
        assertTrue(tile.matches(5))
        assertFalse(tile.matches(1))
    }

    @Test
    fun `double matches its single pip value`() {
        val double = Tile(6, 6)

        assertTrue(double.matches(6))
        assertFalse(double.matches(5))
    }

    @Test
    fun `double is detected`() {
        assertTrue(Tile(6, 6).isDouble())
        assertFalse(Tile(6, 5).isDouble())
    }

    @Test
    fun `pip sum adds both ends`() {
        assertEquals(12, Tile(6, 6).pipSum())
        assertEquals(9, Tile(6, 3).pipSum())
        assertEquals(0, Tile(0, 0).pipSum())
    }

    @Test
    fun `reversed tile swaps its pips`() {
        assertEquals(Tile(5, 3), Tile(3, 5).reversed())
    }
}
