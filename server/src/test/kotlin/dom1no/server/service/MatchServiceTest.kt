package dom1no.server.service

import dom1no.server.domain.BoardEnd
import dom1no.server.domain.MatchConfiguration
import dom1no.server.domain.MatchStatus
import dom1no.server.domain.RejectionReason
import dom1no.server.domain.Tile
import dom1no.server.domain.deckOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MatchServiceTest {
    private val deck = deckOf(
        "66 65 54 43 32 21 10 60 00 55 44 33 22 11 " +
            "02 03 04 05 13 14 15 16 24 25 26 35 36 46",
    )

    @Test
    fun `matches have separate identities and participant-scoped views`() {
        val service = MatchService()
        service.startMatch("first", MatchConfiguration.TWO_PLAYER_INDIVIDUAL, listOf("alice", "bob"), deck)
        service.startMatch("second", MatchConfiguration.TWO_PLAYER_INDIVIDUAL, listOf("alice", "carol"), deck)

        val alice = service.view("first", "alice")!!
        val bob = service.view("first", "bob")!!
        assertEquals("first", alice.matchId)
        assertEquals(0, alice.version)
        assertEquals(1, alice.seatNumber)
        assertEquals(2, bob.seatNumber)
        assertEquals(6, alice.hand.size)
        assertEquals(7, bob.hand.size)
        assertEquals(listOf(6, 7), alice.handCounts)
        assertEquals(14, alice.boneyardCount)
        assertNull(service.view("first", "carol"))
        assertNull(service.view("missing", "alice"))
        assertThrows(IllegalArgumentException::class.java) {
            service.startMatch("first", MatchConfiguration.TWO_PLAYER_INDIVIDUAL, listOf("x", "y"), deck)
        }

        assertTrue(service.place("first", "bob", 0, Tile(6, 0), BoardEnd.RIGHT) is MatchActionResult.Accepted)
        assertEquals(1, service.view("first", "alice")!!.version)
        assertEquals(0, service.view("second", "alice")!!.version)
    }

    @Test
    fun `registration requires exactly one identified player per seat`() {
        val service = MatchService()
        for (configuration in MatchConfiguration.entries) {
            val players = (1..configuration.seatCount).map { "player$it" }
            service.startMatch(configuration.name, configuration, players, deck)
            val view = service.view(configuration.name, players.last())!!
            assertEquals(configuration.seatCount, view.handCounts.size)
            assertEquals(configuration.seatCount, view.seats.size)
            assertEquals(configuration.teamNumberFor(configuration.seatCount), view.seats.last().teamNumber)
        }
        assertThrows(IllegalArgumentException::class.java) {
            service.startMatch("short", MatchConfiguration.THREE_PLAYER_INDIVIDUAL, listOf("a", "b"), deck)
        }
        assertThrows(IllegalArgumentException::class.java) {
            service.startMatch("duplicate", MatchConfiguration.TWO_PLAYER_INDIVIDUAL, listOf("a", "a"), deck)
        }
    }

    @Test
    fun `unknown and non-participant actions cannot affect the match`() {
        val service = MatchService()
        service.startMatch("game", MatchConfiguration.TWO_PLAYER_INDIVIDUAL, listOf("alice", "bob"), deck)
        val before = service.view("game", "alice")

        assertEquals(MatchActionResult.Rejected(ServiceRejection.UNKNOWN_MATCH),
            service.place("other", "bob", 0, Tile(6, 0), BoardEnd.LEFT))
        assertEquals(MatchActionResult.Rejected(ServiceRejection.NOT_PARTICIPANT),
            service.place("game", "intruder", 0, Tile(6, 0), BoardEnd.LEFT))
        assertEquals(MatchActionResult.Rejected(ServiceRejection.NOT_PARTICIPANT),
            service.draw("game", "intruder", 0))
        assertEquals(MatchActionResult.Rejected(ServiceRejection.NOT_PARTICIPANT),
            service.pass("game", "intruder", 0))
        assertNull(service.view("game", "intruder"))
        assertEquals(before, service.view("game", "alice"))
    }

    @Test
    fun `all actions delegate to domain and rejected actions preserve state and version`() {
        val service = MatchService()
        val noSix = deckOf(
            "66 65 54 43 32 21 10 00 11 22 33 44 55 02 " +
                "03 04 06 05 13 14 15 16 24 25 26 35 36 46",
        )
        service.startMatch("game", MatchConfiguration.TWO_PLAYER_INDIVIDUAL, listOf("alice", "bob"), noSix)
        val before = service.view("game", "bob")
        assertEquals(MatchActionResult.Rejected(ServiceRejection.Game(RejectionReason.NOT_YOUR_TURN)),
            service.place("game", "alice", 0, Tile(6, 5), BoardEnd.LEFT))
        assertEquals(MatchActionResult.Rejected(ServiceRejection.Game(RejectionReason.MUST_DRAW)),
            service.pass("game", "bob", 0))
        assertEquals(before, service.view("game", "bob"))

        val drawn = service.draw("game", "bob", 0) as MatchActionResult.Accepted
        assertEquals(listOf(Tile(0, 3), Tile(0, 4), Tile(0, 6)), drawn.drawnTiles)
        assertEquals(1, drawn.state.version)
        assertEquals(2, drawn.state.activeSeatNumber)
        assertEquals(10, drawn.state.hand.size)
        assertEquals(1, service.view("game", "alice")!!.version)
        assertEquals(MatchActionResult.Rejected(ServiceRejection.STALE_VERSION),
            service.place("game", "bob", 0, Tile(0, 6), BoardEnd.RIGHT))
        assertEquals(MatchActionResult.Rejected(ServiceRejection.Game(RejectionReason.PLACEMENT_AVAILABLE)),
            service.draw("game", "bob", 1))
        assertEquals(1, service.view("game", "bob")!!.version)
        assertTrue(service.place("game", "bob", 1, Tile(0, 6), BoardEnd.RIGHT) is MatchActionResult.Accepted)
    }

    @Test
    fun `simultaneous actions with the same version cannot both transition a match`() {
        val service = MatchService()
        service.startMatch("game", MatchConfiguration.TWO_PLAYER_INDIVIDUAL, listOf("alice", "bob"), deck)
        val pool = Executors.newFixedThreadPool(2)
        val gate = CountDownLatch(1)
        try {
            val attempts = (1..2).map {
                pool.submit<MatchActionResult> {
                    gate.await()
                    service.place("game", "bob", 0, Tile(6, 0), BoardEnd.RIGHT)
                }
            }
            gate.countDown()
            val results = attempts.map { it.get(5, TimeUnit.SECONDS) }
            assertEquals(1, results.count { it is MatchActionResult.Accepted })
            assertEquals(1, results.count { it == MatchActionResult.Rejected(ServiceRejection.STALE_VERSION) })
            assertEquals(1, service.view("game", "bob")!!.version)
            assertEquals(2, service.view("game", "bob")!!.board.placedTiles.size)
        } finally {
            pool.shutdownNow()
        }
    }

    @Test
    fun `partnership views identify teams without exposing opponents hands`() {
        val service = MatchService()
        service.startMatch("team", MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, listOf("a", "b", "c", "d"), deck)
        val view = service.view("team", "a")!!
        assertEquals(listOf(1, 2, 1, 2), view.seats.map { it.teamNumber })
        assertEquals(listOf("a", "b", "c", "d"), view.seats.map { it.participantId })
        assertEquals(0, view.boneyardCount)
        assertFalse(view.hand.contains(Tile(6, 0)))
        assertEquals(MatchStatus.ACTIVE, view.status)
    }

    @Test
    fun `a legal block pass advances the turn and a returned view cannot change the match`() {
        val service = MatchService()
        val noSix = deckOf(
            "06 16 26 00 11 22 33 36 46 56 44 55 01 02 " +
                "66 03 04 05 12 13 14 15 23 24 25 34 35 45",
        )
        service.startMatch("team", MatchConfiguration.FOUR_PLAYER_PARTNERSHIP, listOf("a", "b", "c", "d"), noSix)
        val before = service.view("team", "d")!!
        assertEquals(4, before.activeSeatNumber)

        runCatching { (before.seats as MutableList<MatchParticipant>).clear() }
        runCatching { (before.hand as MutableList<Tile>).clear() }
        runCatching { (before.board.placedTiles as MutableList<*>).clear() }
        assertEquals(4, service.view("team", "d")!!.seats.size)
        assertEquals(7, service.view("team", "d")!!.hand.size)
        assertEquals(1, service.view("team", "d")!!.board.placedTiles.size)

        val passed = service.pass("team", "d", 0) as MatchActionResult.Accepted
        assertEquals(1, passed.state.version)
        assertEquals(1, passed.state.activeSeatNumber)
        assertEquals(MatchActionResult.Rejected(ServiceRejection.STALE_VERSION),
            service.pass("team", "d", 0))
        assertEquals(MatchActionResult.Rejected(ServiceRejection.Game(RejectionReason.NOT_YOUR_TURN)),
            service.pass("team", "d", 1))
        assertEquals(1, service.view("team", "a")!!.version)
    }
}
