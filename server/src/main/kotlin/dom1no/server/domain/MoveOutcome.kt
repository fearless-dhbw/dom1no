package dom1no.server.domain

/** The result of a match action, so the service can confirm it or explain a rejection. */
sealed interface MoveOutcome {
    data object Accepted : MoveOutcome
    data class Rejected(val reason: RejectionReason) : MoveOutcome
}

enum class RejectionReason {
    MATCH_NOT_ACTIVE,
    NOT_YOUR_TURN,
    TILE_NOT_IN_HAND,
    ILLEGAL_PLACEMENT,
}
