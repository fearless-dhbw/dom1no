package dom1no.server.domain

import kotlin.random.Random

enum class MatchStatus { WAITING, ACTIVE, FINISHED }

/**
 * One authoritative match: seats and hands, the boneyard, the board and whose turn it is.
 *
 * Starting a match shuffles and deals seven tiles per seat, selects the opener under the game
 * rules, applies the opening placement and passes the turn to the next seat. The opener's
 * placement counts as their turn.
 *
 * Callers read the board and seats through immutable snapshots; the only way to change a match is
 * through [applyPlacement], [drawUntilPlayable] and [recordPass].
 */
class Match private constructor(
    val configuration: MatchConfiguration,
    private val boardState: Board,
    private val seatStates: List<Seat>,
    private val remainingTiles: MutableList<Tile>,
    val openingTile: Tile,
    val openerSeatNumber: Int,
    initialActiveSeatIndex: Int,
) {

    var activeSeatIndex: Int = initialActiveSeatIndex
        private set

    var status: MatchStatus = MatchStatus.ACTIVE
        private set

    var consecutivePasses: Int = 0
        private set

    /** Undealt tiles in draw order. */
    val boneyard: List<Tile> get() = remainingTiles.toList()

    /** Read-only snapshot of the board. */
    val board: BoardSnapshot get() = boardState.snapshot()

    /** Read-only snapshots of the seats in seat order. */
    val seats: List<SeatSnapshot> get() = seatStates.map { it.snapshot() }

    /** Read-only snapshot of the active seat. */
    val activeSeat: SeatSnapshot get() = seatStates[activeSeatIndex].snapshot()

    /**
     * Applies the active seat's placement. Every rejection is checked before any state changes, so
     * a rejected placement leaves the board, hands, turn and pass count untouched.
     *
     * An accepted placement that empties the hand finishes the match immediately and does not
     * advance the turn; the result then names that seat or team as the winner.
     */
    fun applyPlacement(seatIndex: Int, tile: Tile, end: BoardEnd): MoveOutcome {
        require(seatIndex in seatStates.indices) { "No seat $seatIndex" }

        if (status != MatchStatus.ACTIVE) {
            return MoveOutcome.Rejected(RejectionReason.MATCH_NOT_ACTIVE)
        }
        if (seatIndex != activeSeatIndex) {
            return MoveOutcome.Rejected(RejectionReason.NOT_YOUR_TURN)
        }

        val seat = seatStates[seatIndex]
        if (!seat.hasTile(tile)) {
            return MoveOutcome.Rejected(RejectionReason.TILE_NOT_IN_HAND)
        }
        if (!boardState.canPlace(tile, end)) {
            return MoveOutcome.Rejected(RejectionReason.ILLEGAL_PLACEMENT)
        }

        boardState.place(tile, end)
        seat.removeTile(tile)
        consecutivePasses = 0

        if (seat.hand.isEmpty()) {
            status = MatchStatus.FINISHED
        } else {
            activeSeatIndex = (activeSeatIndex + 1) % seatStates.size
        }
        return MoveOutcome.Accepted
    }

    /**
     * Draws tiles for the active seat until a tile that can be placed is drawn or the boneyard is
     * empty. The active seat keeps the turn either way, and drawing does not change the pass count.
     *
     * Drawing is rejected when the seat already has a legal placement or when no tiles remain.
     */
    fun drawUntilPlayable(seatIndex: Int): List<Tile> {
        require(seatIndex in seatStates.indices) { "No seat $seatIndex" }

        if (status != MatchStatus.ACTIVE) {
            throw MoveRejectedException(RejectionReason.MATCH_NOT_ACTIVE)
        }
        if (seatIndex != activeSeatIndex) {
            throw MoveRejectedException(RejectionReason.NOT_YOUR_TURN)
        }

        val seat = seatStates[seatIndex]
        if (canPlaceAnyTile(seat)) {
            throw MoveRejectedException(RejectionReason.PLACEMENT_AVAILABLE)
        }
        if (remainingTiles.isEmpty()) {
            throw MoveRejectedException(RejectionReason.NOTHING_TO_DRAW)
        }

        val drawn = mutableListOf<Tile>()
        while (remainingTiles.isNotEmpty()) {
            val tile = remainingTiles.removeAt(0)
            seat.addTile(tile)
            drawn.add(tile)
            if (boardState.canPlace(tile, BoardEnd.LEFT) || boardState.canPlace(tile, BoardEnd.RIGHT)) break
        }
        return drawn
    }

    /**
     * Records the active seat's pass. A pass is legal only when the seat has no placement and no
     * tile remains to draw; it advances the turn unless every participant has now passed, which
     * ends the match as blocked.
     */
    fun recordPass(seatIndex: Int): MoveOutcome {
        require(seatIndex in seatStates.indices) { "No seat $seatIndex" }

        if (status != MatchStatus.ACTIVE) {
            return MoveOutcome.Rejected(RejectionReason.MATCH_NOT_ACTIVE)
        }
        if (seatIndex != activeSeatIndex) {
            return MoveOutcome.Rejected(RejectionReason.NOT_YOUR_TURN)
        }

        val seat = seatStates[seatIndex]
        if (canPlaceAnyTile(seat)) {
            return MoveOutcome.Rejected(RejectionReason.PLACEMENT_AVAILABLE)
        }
        if (remainingTiles.isNotEmpty()) {
            return MoveOutcome.Rejected(RejectionReason.MUST_DRAW)
        }

        consecutivePasses += 1
        if (consecutivePasses == seatStates.size) {
            status = MatchStatus.FINISHED
        } else {
            activeSeatIndex = (activeSeatIndex + 1) % seatStates.size
        }
        return MoveOutcome.Accepted
    }

    private fun canPlaceAnyTile(seat: Seat): Boolean =
        seat.hand.any { boardState.canPlace(it, BoardEnd.LEFT) || boardState.canPlace(it, BoardEnd.RIGHT) }

    /** The result of a finished match; null while the match is still active. */
    val result: MatchResult?
        get() = if (status == MatchStatus.FINISHED) evaluateOutcome() else null

    /** Computes the explicit result and end reason of a finished match. */
    fun evaluateOutcome(): MatchResult {
        check(status == MatchStatus.FINISHED) { "Match is not finished" }

        val emptyHandSeat = seatStates.firstOrNull { it.hand.isEmpty() }
        return if (emptyHandSeat != null) emptyHandResult(emptyHandSeat) else blockedResult()
    }

    private fun emptyHandResult(emptySeat: Seat): MatchResult {
        val winningTeam = if (configuration.isPartnership) emptySeat.teamNumber else null
        val outcomes = seatStates.associate { seat ->
            val wins = if (configuration.isPartnership) {
                seat.teamNumber == winningTeam
            } else {
                seat.seatNumber == emptySeat.seatNumber
            }
            seat.seatNumber to if (wins) ParticipantOutcome.WIN else ParticipantOutcome.LOSS
        }
        return MatchResult(EndReason.EMPTY_HAND, outcomes)
    }

    private fun blockedResult(): MatchResult {
        val pips = seatStates.associate { it.seatNumber to it.remainingPips() }
        val outcomes = if (configuration.isPartnership) {
            partnershipOutcomes(pips)
        } else {
            individualOutcomes(pips)
        }
        return MatchResult(EndReason.BLOCKED, outcomes, pips)
    }

    private fun individualOutcomes(pips: Map<Int, Int>): Map<Int, ParticipantOutcome> {
        val lowest = pips.values.min()
        val lowestSeats = pips.filterValues { it == lowest }.keys
        return pips.mapValues { (seatNumber, _) ->
            when {
                seatNumber !in lowestSeats -> ParticipantOutcome.LOSS
                lowestSeats.size == 1 -> ParticipantOutcome.WIN
                else -> ParticipantOutcome.DRAW
            }
        }
    }

    private fun partnershipOutcomes(pips: Map<Int, Int>): Map<Int, ParticipantOutcome> {
        val teamTotals = seatStates.groupBy { requireNotNull(it.teamNumber) }
            .mapValues { (_, teamSeats) -> teamSeats.sumOf { pips.getValue(it.seatNumber) } }
        val lowest = teamTotals.values.min()
        val lowestTeams = teamTotals.filterValues { it == lowest }.keys
        return seatStates.associate { seat ->
            val team = requireNotNull(seat.teamNumber)
            seat.seatNumber to when {
                team !in lowestTeams -> ParticipantOutcome.LOSS
                lowestTeams.size == 1 -> ParticipantOutcome.WIN
                else -> ParticipantOutcome.DRAW
            }
        }
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
                boardState = Board().apply { place(openingTile, BoardEnd.LEFT) },
                seatStates = seats,
                remainingTiles = deck.drop(configuration.seatCount * HAND_SIZE).toMutableList(),
                openingTile = openingTile,
                openerSeatNumber = opener.seatNumber,
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
