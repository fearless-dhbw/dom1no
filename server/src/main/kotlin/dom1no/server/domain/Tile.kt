package dom1no.server.domain

/** The order-independent identity of one tile from the double-six set. */
class Tile private constructor(val lowPip: Int, val highPip: Int) {

    init {
        require(lowPip in MIN_PIP..MAX_PIP) { "Pip $lowPip is outside $MIN_PIP..$MAX_PIP" }
        require(highPip in MIN_PIP..MAX_PIP) { "Pip $highPip is outside $MIN_PIP..$MAX_PIP" }
    }

    /** True when either pip equals [endValue], so the tile can touch an open end showing it. */
    fun matches(endValue: Int): Boolean = lowPip == endValue || highPip == endValue

    fun pipSum(): Int = lowPip + highPip

    fun isDouble(): Boolean = lowPip == highPip

    override fun equals(other: Any?): Boolean =
        this === other || other is Tile && lowPip == other.lowPip && highPip == other.highPip

    override fun hashCode(): Int = 31 * lowPip + highPip

    override fun toString(): String = "$lowPip|$highPip"

    companion object {
        const val MIN_PIP = 0
        const val MAX_PIP = 6

        /** Either pip order creates the same tile. Orientation belongs to [PlacedTile]. */
        operator fun invoke(firstPip: Int, secondPip: Int): Tile =
            Tile(minOf(firstPip, secondPip), maxOf(firstPip, secondPip))
    }
}
