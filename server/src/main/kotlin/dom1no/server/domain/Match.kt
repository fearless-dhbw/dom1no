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
    val activeSeatIndex: Int,
) {

    var status: MatchStatus = MatchStatus.ACTIVE
        private set

    var consecutivePasses: Int = 0
        private set

    /** Undealt tiles in draw order. */
    val boneyard: List<Tile> get() = remainingTiles.toList()

    val activeSeat: Seat get() = seats[activeSeatIndex]

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
                activeSeatIndex = opener.seatNumber % configuration.seatCount,
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
