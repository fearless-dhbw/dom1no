package dom1no.server.domain

import org.junit.jupiter.api.Assertions.assertEquals

/** Parses a deck spec such as `"36 55 13"` into tiles, preserving deal order. */
internal fun deckOf(spec: String): List<Tile> =
    spec.trim().split(Regex("\\s+")).map(::tileOf)

/**
 * Replays a recorded natural game. Each line is one action:
 * `p<seat> <tile> <L|R>` places, `d<seat> <tile>,<tile>` draws and `x<seat>` passes.
 *
 * Every action is asserted accepted, so a fixture cannot silently drift from the rules.
 */
internal fun replay(match: Match, script: String) {
    for (line in script.trim().lines()) {
        val parts = line.trim().split(Regex("\\s+"))
        if (parts[0].isEmpty()) continue

        val seat = parts[0].drop(1).toInt()
        when (parts[0].first()) {
            'p' -> assertEquals(
                MoveOutcome.Accepted,
                match.applyPlacement(seat, tileOf(parts[1]), endOf(parts[2])),
            )
            'd' -> assertEquals(
                parts[1].split(",").map(::tileOf),
                match.drawUntilPlayable(seat),
            )
            'x' -> assertEquals(MoveOutcome.Accepted, match.recordPass(seat))
            else -> error("Unknown action ${parts[0]}")
        }
    }
}

private fun tileOf(token: String): Tile = Tile(token[0] - '0', token[1] - '0')

private fun endOf(token: String): BoardEnd = when (token) {
    "L" -> BoardEnd.LEFT
    "R" -> BoardEnd.RIGHT
    else -> error("Unknown end $token")
}
