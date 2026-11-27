package dom1no.server.domain

/** The one double-six set used by every match configuration. */
object DominoSet {

    /** The 28 distinct unordered pip pairs from 0|0 through 6|6. */
    fun standardDoubleSix(): List<Tile> = buildList {
        for (left in Tile.MIN_PIP..Tile.MAX_PIP) {
            for (right in left..Tile.MAX_PIP) {
                add(Tile(left, right))
            }
        }
    }
}
