package dom1no.server.domain

/**
 * One domino tile as an ordered pair of pips.
 *
 * The order is meaningful only for a tile that has been placed on the board: it records which
 * pip touches the chain. Tiles in the standard set are created in canonical order, so equal
 * tiles compare equal there.
 */
data class Tile(val leftPip: Int, val rightPip: Int) {

    init {
        require(leftPip in MIN_PIP..MAX_PIP) { "Left pip $leftPip is outside $MIN_PIP..$MAX_PIP" }
        require(rightPip in MIN_PIP..MAX_PIP) { "Right pip $rightPip is outside $MIN_PIP..$MAX_PIP" }
    }

    /** True when either pip equals [endValue], so the tile can touch an open end showing it. */
    fun matches(endValue: Int): Boolean = leftPip == endValue || rightPip == endValue

    fun pipSum(): Int = leftPip + rightPip

    fun isDouble(): Boolean = leftPip == rightPip

    /** The same tile with its pips swapped, used to orient it against an open end. */
    fun reversed(): Tile = Tile(rightPip, leftPip)

    override fun toString(): String = "$leftPip|$rightPip"

    companion object {
        const val MIN_PIP = 0
        const val MAX_PIP = 6
    }
}
