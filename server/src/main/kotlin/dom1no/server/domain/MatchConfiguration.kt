package dom1no.server.domain

/** The three agreed match configurations. Seat count and team membership are fixed, not chosen. */
enum class MatchConfiguration(val seatCount: Int) {
    TWO_PLAYER_INDIVIDUAL(2),
    THREE_PLAYER_INDIVIDUAL(3),
    FOUR_PLAYER_PARTNERSHIP(4);

    val isPartnership: Boolean get() = this == FOUR_PLAYER_PARTNERSHIP

    /** Opposite seats are partners: seats 1 and 3 against seats 2 and 4. */
    fun teamNumberFor(seatNumber: Int): Int? = when {
        !isPartnership -> null
        seatNumber % 2 == 1 -> 1
        else -> 2
    }
}
