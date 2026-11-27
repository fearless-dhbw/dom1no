package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DominoSetTest {

    @Test
    fun `standard set contains the 28 unique double-six tiles`() {
        val tiles = DominoSet.standardDoubleSix()

        assertEquals(28, tiles.size)
        assertEquals(28, tiles.toSet().size)
    }

    @Test
    fun `standard set contains every unordered pip pair`() {
        val tiles = DominoSet.standardDoubleSix().toSet()

        for (left in 0..6) {
            for (right in left..6) {
                assertTrue(tiles.contains(Tile(left, right)), "Missing tile $left|$right")
            }
        }
    }

    @Test
    fun `tiles from the set are found by either pip order`() {
        assertTrue(DominoSet.standardDoubleSix().contains(Tile(5, 3)))
    }
}
