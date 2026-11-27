package dom1no.server.domain

/** A read-only view of one seat at a point in time. */
data class SeatSnapshot(
    val seatNumber: Int,
    val teamNumber: Int?,
    val hand: List<Tile>,
) {
    fun hasTile(tile: Tile): Boolean = hand.contains(tile)

    fun remainingPips(): Int = hand.sumOf { it.pipSum() }
}

/** One participant's seat, its fixed team and the tiles currently in hand. */
internal class Seat(
    val seatNumber: Int,
    val teamNumber: Int?,
    hand: List<Tile>,
) {

    private val tiles = hand.toMutableList()

    val hand: List<Tile> get() = tiles.toList()

    fun hasTile(tile: Tile): Boolean = tiles.contains(tile)

    fun addTile(tile: Tile) {
        tiles.add(tile)
    }

    fun removeTile(tile: Tile) {
        require(tiles.remove(tile)) { "Seat $seatNumber does not hold $tile" }
    }

    fun remainingPips(): Int = tiles.sumOf { it.pipSum() }

    fun snapshot(): SeatSnapshot = SeatSnapshot(seatNumber, teamNumber, hand)
}
