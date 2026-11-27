package dom1no.server.domain

/** The two ends at which a tile can be added to the chain. */
enum class BoardEnd { LEFT, RIGHT }

/** Raised when a placement does not match the chosen open end. The board is left unchanged. */
class IllegalPlacementException(message: String) : IllegalArgumentException(message)

/** The orientation of a tile in the board's left-to-right chain. */
data class PlacedTile(val leftPip: Int, val rightPip: Int) {
    val tile: Tile = Tile(leftPip, rightPip)
}

/**
 * The single chain of placed tiles, with an open pip value at each end.
 *
 * Tiles are stored oriented towards the chain, so `placedTiles.first().leftPip` is the left open
 * end and `placedTiles.last().rightPip` is the right open end. A rejected placement throws before
 * the chain is touched, which keeps the accepted board state intact.
 */
class Board {

    private val chain = mutableListOf<PlacedTile>()

    /** Oriented tiles from the left open end to the right open end. */
    val placedTiles: List<PlacedTile> get() = chain.toList()

    val openLeft: Int? get() = chain.firstOrNull()?.leftPip

    val openRight: Int? get() = chain.lastOrNull()?.rightPip

    fun canPlace(tile: Tile, end: BoardEnd): Boolean =
        if (chain.isEmpty()) true else tile.matches(openValueAt(end))

    fun place(tile: Tile, end: BoardEnd) {
        if (chain.isEmpty()) {
            chain.add(PlacedTile(tile.highPip, tile.lowPip))
            return
        }

        val openValue = openValueAt(end)
        if (!tile.matches(openValue)) {
            throw IllegalPlacementException(
                "Tile $tile does not match the open $end end $openValue (open ends $openLeft and $openRight)."
            )
        }

        val oriented = orientTowards(tile, openValue, end)
        if (end == BoardEnd.LEFT) chain.add(0, oriented) else chain.add(oriented)
    }

    private fun openValueAt(end: BoardEnd): Int = when (end) {
        BoardEnd.LEFT -> openLeft!!
        BoardEnd.RIGHT -> openRight!!
    }

    /** Puts the matching pip next to the chain and exposes the other pip. */
    private fun orientTowards(tile: Tile, openValue: Int, end: BoardEnd): PlacedTile {
        val otherPip = if (tile.lowPip == openValue) tile.highPip else tile.lowPip
        return if (end == BoardEnd.LEFT) PlacedTile(otherPip, openValue)
        else PlacedTile(openValue, otherPip)
    }
}
