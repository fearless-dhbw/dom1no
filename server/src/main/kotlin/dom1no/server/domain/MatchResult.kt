package dom1no.server.domain

/** Why a match ended. */
enum class EndReason { EMPTY_HAND, BLOCKED }

/** One participant's result in a finished match. */
enum class ParticipantOutcome { WIN, LOSS, DRAW }

/**
 * The result of a finished match. [remainingPips] is populated for a blocked match and empty when
 * a hand was emptied, because only a blocked result is decided by pip totals.
 */
data class MatchResult(
    val endReason: EndReason,
    val outcomes: Map<Int, ParticipantOutcome>,
    val remainingPips: Map<Int, Int> = emptyMap(),
) {
    val winners: Set<Int> get() = outcomes.filterValues { it == ParticipantOutcome.WIN }.keys

    /** True when no seat or team finished ahead of the others. */
    val isDraw: Boolean get() = winners.isEmpty()
}
