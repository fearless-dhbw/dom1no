package dom1no.server.service

import dom1no.server.domain.BoardEnd
import dom1no.server.domain.BoardSnapshot
import dom1no.server.domain.Match
import dom1no.server.domain.MatchConfiguration
import dom1no.server.domain.MatchResult
import dom1no.server.domain.MatchStatus
import dom1no.server.domain.MoveOutcome
import dom1no.server.domain.MoveRejectedException
import dom1no.server.domain.RejectionReason
import dom1no.server.domain.Tile
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * One participant's projection of a match. [hand] is the caller's own hand; [handCounts] is in
 * seat order and contains sizes only, so opponents' tiles and the boneyard order stay private.
 */
data class MatchView(
    val matchId: String,
    val version: Long,
    val configuration: MatchConfiguration,
    val seatNumber: Int,
    val seats: List<MatchParticipant>,
    val hand: List<Tile>,
    val handCounts: List<Int>,
    val board: BoardSnapshot,
    val boneyardCount: Int,
    val activeSeatNumber: Int,
    val consecutivePasses: Int,
    val status: MatchStatus,
    val result: MatchResult?,
)

data class MatchParticipant(val participantId: String, val seatNumber: Int, val teamNumber: Int?)

sealed interface ServiceRejection {
    data object UNKNOWN_MATCH : ServiceRejection
    data object NOT_PARTICIPANT : ServiceRejection
    data object STALE_VERSION : ServiceRejection
    data class Game(val reason: RejectionReason) : ServiceRejection
}

sealed interface MatchActionResult {
    data class Accepted(val state: MatchView, val drawnTiles: List<Tile> = emptyList()) : MatchActionResult
    data class Rejected(val reason: ServiceRejection) : MatchActionResult
}

/**
 * In-memory authority for preconfigured matches. An upstream adapter must supply an authenticated
 * participant ID; this service does not authenticate a caller or define a wire format.
 *
 * An expected version is required for every action, including draws that retain the turn. Each
 * match is locked independently across validation, mutation, version increment and state projection.
 */
class MatchService {
    private class RunningMatch(val match: Match, val participants: List<String>) {
        val seatByParticipant = participants.withIndex().associate { (index, id) -> id to index }
        var version = 0L

        fun view(matchId: String, seatIndex: Int): MatchView {
            val seats = match.seats
            return MatchView(
                matchId = matchId,
                version = version,
                configuration = match.configuration,
                seatNumber = seatIndex + 1,
                seats = seats.mapIndexed { index, seat ->
                    MatchParticipant(participants[index], seat.seatNumber, seat.teamNumber)
                },
                hand = seats[seatIndex].hand,
                handCounts = seats.map { it.hand.size },
                board = match.board,
                boneyardCount = match.boneyard.size,
                activeSeatNumber = match.activeSeat.seatNumber,
                consecutivePasses = match.consecutivePasses,
                status = match.status,
                result = match.result,
            )
        }
    }

    private val matches = ConcurrentHashMap<String, RunningMatch>()

    fun startMatch(
        matchId: String,
        configuration: MatchConfiguration,
        participants: List<String>,
        random: Random = Random.Default,
    ) {
        register(matchId, configuration, participants, Match.start(configuration, random))
    }

    /** Deterministic deal for tests; production starts with [startMatch] and a shuffled deck. */
    internal fun startMatch(
        matchId: String,
        configuration: MatchConfiguration,
        participants: List<String>,
        deck: List<Tile>,
    ) {
        register(matchId, configuration, participants, Match.start(configuration, deck))
    }

    private fun register(matchId: String, configuration: MatchConfiguration, participants: List<String>, match: Match) {
        require(matchId.isNotBlank()) { "Match ID is required" }
        require(participants.size == configuration.seatCount &&
            participants.all { it.isNotBlank() } && participants.distinct().size == participants.size) {
            "Provide one distinct participant ID per seat"
        }
        require(matches.putIfAbsent(matchId, RunningMatch(match, participants.toList())) == null) {
            "Match $matchId already exists"
        }
    }

    /** Returns null for an unknown match or a caller who does not participate in it. */
    fun view(matchId: String, participantId: String): MatchView? {
        val running = matches[matchId] ?: return null
        return synchronized(running) {
            val seatIndex = running.seatByParticipant[participantId] ?: return@synchronized null
            running.view(matchId, seatIndex)
        }
    }

    fun place(
        matchId: String,
        participantId: String,
        expectedVersion: Long,
        tile: Tile,
        end: BoardEnd,
    ): MatchActionResult = act(matchId, participantId, expectedVersion) { match, seatIndex ->
        when (val outcome = match.applyPlacement(seatIndex, tile, end)) {
            MoveOutcome.Accepted -> emptyList()
            is MoveOutcome.Rejected -> throw MoveRejectedException(outcome.reason)
        }
    }

    fun draw(matchId: String, participantId: String, expectedVersion: Long): MatchActionResult =
        act(matchId, participantId, expectedVersion) { match, seatIndex ->
            match.drawUntilPlayable(seatIndex)
        }

    fun pass(matchId: String, participantId: String, expectedVersion: Long): MatchActionResult =
        act(matchId, participantId, expectedVersion) { match, seatIndex ->
            when (val outcome = match.recordPass(seatIndex)) {
                MoveOutcome.Accepted -> emptyList()
                is MoveOutcome.Rejected -> throw MoveRejectedException(outcome.reason)
            }
        }

    private inline fun act(
        matchId: String,
        participantId: String,
        expectedVersion: Long,
        action: (Match, Int) -> List<Tile>,
    ): MatchActionResult {
        val running = matches[matchId] ?: return MatchActionResult.Rejected(ServiceRejection.UNKNOWN_MATCH)
        return synchronized(running) {
            val seatIndex = running.seatByParticipant[participantId]
                ?: return@synchronized MatchActionResult.Rejected(ServiceRejection.NOT_PARTICIPANT)
            if (expectedVersion != running.version) {
                return@synchronized MatchActionResult.Rejected(ServiceRejection.STALE_VERSION)
            }
            val drawn = try {
                action(running.match, seatIndex)
            } catch (rejection: MoveRejectedException) {
                return@synchronized MatchActionResult.Rejected(ServiceRejection.Game(rejection.reason))
            }
            running.version++
            MatchActionResult.Accepted(running.view(matchId, seatIndex), drawn)
        }
    }
}
