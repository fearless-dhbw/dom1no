package dom1no.server.domain

import kotlin.random.Random

enum class MatchStatus { WAITING, ACTIVE, FINISHED }

/**
 * One authoritative match: seats and hands, the boneyard, the board and whose turn it is.
 *
 * Starting a match shuffles and deals seven tiles per seat, selects the opener under the game
 * rules, applies the opening placement and passes the turn to the next seat. The opener's
 * placement counts as their turn.
 */
class Match private constructor(
    val configuration: MatchConfiguration,
    val seats: List<Seat>,
    val board: Board,
    private val remainingTiles: MutableList<Tile>,
    val openingTile: Tile,
    val opener: Seat,
    initialActiveSeatIndex: Int,
) {

    var activeSeatIndex: Int = initialActiveSeatIndex
        private set

    var status: MatchStatus = MatchStatus.ACTIVE
        private set

    var consecutivePasses: Int = 0
        internal set

    /** Undealt tiles in draw order. */
    val boneyard: List<Tile> get() = remainingTiles.toList()

    val activeSeat: Seat get() = seats[activeSeatIndex]

    /**
     * Applies the active seat's placement. Every rejection is checked before any state changes, so
     * a rejected placement leaves the board, hands, turn and pass count untouched.
     *
     * An accepted placement that empties the hand finishes the match immediately and does not
     * advance the turn. Winner and blocked-result reporting belong to the terminal-result step.
     */
    fun applyPlacement(seatIndex: Int, tile: Tile, end: BoardEnd): MoveOutcome {
        require(seatIndex in seats.indices) { "No seat $seatIndex" }

        if (status != MatchStatus.ACTIVE) {
            return MoveOutcome.Rejected(RejectionReason.MATCH_NOT_ACTIVE)
        }
        if (seatIndex != activeSeatIndex) {
            return MoveOutcome.Rejected(RejectionReason.NOT_YOUR_TURN)
        }

        val seat = seats[seatIndex]
        if (!seat.hasTile(tile)) {
            return MoveOutcome.Rejected(RejectionReason.TILE_NOT_IN_HAND)
        }
        if (!board.canPlace(tile, end)) {
            return MoveOutcome.Rejected(RejectionReason.ILLEGAL_PLACEMENT)
        }

        board.place(tile, end)
        seat.removeTile(tile)
        consecutivePasses = 0

        if (seat.hand.isEmpty()) {
            status = MatchStatus.FINISHED
        } else {
            activeSeatIndex = (activeSeatIndex + 1) % seats.size
        }
        return MoveOutcome.Accepted
    }

    companion object {
        private const val HAND_SIZE = 7

        /** Shuffles the double-six set with [random] and starts the match. */
        fun start(configuration: MatchConfiguration, random: Random = Random.Default): Match =
            start(configuration, DominoSet.standardDoubleSix().shuffled(random))

        /** Starts from an already-shuffled [deck] in deal order, which fixes a reproducible state. */
        fun start(configuration: MatchConfiguration, deck: List<Tile>): Match {
            requireDealable(deck)

            val seats = (0 until configuration.seatCount).map { index ->
                val seatNumber = index + 1
                val hand = deck.subList(index * HAND_SIZE, (index + 1) * HAND_SIZE)
                Seat(seatNumber, configuration.teamNumberFor(seatNumber), hand)
            }

            val openingTile = selectOpeningTile(seats)
            val opener = seats.single { it.hasTile(openingTile) }
            opener.removeTile(openingTile)

            return Match(
                configuration = configuration,
                seats = seats,
                board = Board().apply { place(openingTile, BoardEnd.LEFT) },
                remainingTiles = deck.drop(configuration.seatCount * HAND_SIZE).toMutableList(),
                openingTile = openingTile,
                opener = opener,
                initialActiveSeatIndex = opener.seatNumber % configuration.seatCount,
            )
        }

        /**
         * The highest dealt double opens; otherwise the greatest pip total, with the higher end
         * breaking a tie. A partnership deal contains every tile, so its highest double is 6|6.
         */
        private fun selectOpeningTile(seats: List<Seat>): Tile {
            val dealt = seats.flatMap { it.hand }
            val doubles = dealt.filter { it.isDouble() }
            return if (doubles.isNotEmpty()) {
                doubles.maxBy { it.highPip }
            } else {
                dealt.maxWith(compareBy<Tile> { it.pipSum() }.thenBy { it.highPip })
            }
        }

        private fun requireDealable(deck: List<Tile>) {
            val expected = DominoSet.standardDoubleSix().toSet()
            require(deck.size == expected.size && deck.toSet() == expected) {
                "A match needs the ${expected.size} distinct double-six tiles"
            }
        }
    }
}
