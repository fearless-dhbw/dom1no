package dom1no.server.acceptance

/** Recorded legal games; scripts stop just before the action under test. Seat indices are zero-based. */
internal data class RecordedGame(val deck: String, val prelude: String)

internal object DUC01Games {
    val placementAfterPass = RecordedGame(
        "22 02 06 44 11 34 04 56 46 03 26 23 14 45 13 66 00 01 33 35 12 05 55 15 16 24 36 25",
        """
        p0 06 L
        p1 26 R
        p2 00 L
        p0 04 L
        p1 46 L
        p2 12 R
        p0 11 R
        p1 14 R
        d2 05,55,15,16
        p2 16 L
        p0 44 R
        p1 45 R
        p2 55 R
        d0 24,36,25
        p0 25 R
        p1 23 R
        p2 33 R
        p0 34 R
        x1
        p2 15 L
        p0 24 R
        p1 56 L
        x2
        """,
    )

    val stockExhaustion = RecordedGame(
        "01 46 14 36 04 66 33 15 34 23 55 24 00 44 03 02 35 06 11 56 22 25 16 12 05 26 13 45",
        """
        d1 25,16
        p1 16 L
        p2 11 L
        p0 14 L
        p1 34 L
        p2 56 R
        p0 36 L
        p1 15 R
        p2 06 L
        p0 04 L
        p1 24 L
        p2 02 L
        p0 01 L
        d1 12
        p1 12 L
        p2 22 L
        d0 05,26
        p0 26 L
        d1 13
        p1 13 R
        p2 03 R
        p0 05 R
        p1 25 R
        """,
    )

    val blockedTeamWin = RecordedGame(
        "03 24 13 25 26 04 22 55 02 16 06 01 34 33 35 45 14 44 12 56 23 15 36 11 66 05 46 00",
        """
        p0 26 L
        p1 02 L
        p2 56 R
        p3 05 L
        p0 25 L
        p1 55 R
        p2 45 R
        p3 46 R
        p0 22 L
        p1 06 R
        p2 23 L
        p3 00 R
        p0 04 R
        p1 34 L
        p2 14 L
        p3 11 L
        p0 24 R
        p1 01 L
        p2 12 R
        p3 15 R
        p0 03 L
        p1 33 L
        p2 35 L
        x3
        x0
        x1
        """,
    )

    val blockedTeamTie = RecordedGame(
        "33 13 56 22 12 24 01 06 05 25 16 14 04 00 45 35 55 23 02 46 44 34 11 03 36 26 66 15",
        """
        p0 56 L
        p1 25 L
        p2 23 L
        p3 03 L
        p0 01 L
        p1 06 R
        p2 02 R
        p3 11 L
        p0 12 L
        x1
        x2
        p3 26 L
        p0 22 R
        p1 16 L
        x2
        p3 15 L
        p0 24 R
        p1 05 L
        p2 45 R
        x3
        x0
        p1 04 L
        p2 46 L
        p3 36 L
        p0 33 L
        x1
        p2 35 L
        x3
        x0
        x1
        p2 55 L
        x3
        x0
        x1
        """,
    )

    val blockedTwoPlayers = RecordedGame(
        "00 15 12 46 11 05 55 34 14 23 45 02 56 66 04 44 35 22 03 16 26 33 25 24 06 01 13 36",
        """
        p0 46 L
        p1 34 L
        d0 04,44,35
        p0 35 L
        p1 45 L
        p0 44 L
        p1 56 R
        p0 55 R
        p1 14 L
        p0 11 L
        d1 22,03,16
        p1 16 L
        p0 15 R
        d1 26
        p1 26 L
        p0 12 L
        d1 33,25,24,06,01
        p1 01 L
        p0 04 L
        p1 24 L
        d0 13
        p0 13 R
        p1 22 L
        d0 36
        p0 36 R
        p1 23 L
        x0
        p1 33 L
        x0
        p1 03 L
        p0 00 L
        p1 06 L
        x0
        """,
    )

    val blockedThreePlayers = RecordedGame(
        "33 55 16 36 46 13 00 45 04 11 03 24 06 56 34 35 12 02 66 22 05 01 26 23 14 15 25 44",
        """
        p0 46 L
        p1 24 L
        p2 12 L
        p0 36 R
        p1 03 R
        p2 02 R
        p0 16 L
        p1 56 L
        p2 22 R
        p0 55 L
        p1 45 L
        p2 34 L
        p0 33 L
        d1 01,26
        p1 26 R
        p2 35 L
        d0 23,14,15
        p0 15 L
        p1 11 L
        d2 25,44
        x2
        p0 13 L
        p1 06 R
        p2 05 R
        p0 23 L
        x1
        p2 25 L
        x0
        x1
        """,
    )

    val emptyTwoPlayers = RecordedGame(
        "12 03 14 46 04 01 06 23 16 02 24 66 11 34 15 26 00 36 13 25 44 35 56 45 55 05 33 22",
        """
        p0 46 L
        p1 16 R
        p0 01 R
        p1 34 L
        p0 04 R
        p1 24 R
        p0 12 R
        p1 23 L
        p0 14 R
        p1 02 L
        p0 06 L
        d1 15,26
        p1 26 L
        d0 00,36,13,25
        p0 25 L
        p1 15 L
        p0 13 L
        d1 44
        p1 44 R
        p0 03 L
        d1 35,56,45
        p1 45 R
        p0 00 L
        p1 56 R
        """,
    )

    val emptyThreePlayers = RecordedGame(
        "03 14 13 35 15 23 16 36 24 66 55 02 04 06 45 33 11 01 05 00 56 26 44 46 22 12 25 34",
        """
        p2 56 L
        p0 15 L
        p1 06 R
        p2 11 L
        p0 14 L
        p1 02 R
        p2 45 L
        p0 23 R
        p1 55 L
        p2 33 R
        p0 03 R
        p1 04 R
        p2 05 L
        d0 26,44
        p0 44 R
        p1 24 R
        p2 00 L
        p0 26 R
        """,
    )

    val emptyPartnership = RecordedGame(
        "45 05 15 35 12 13 00 44 26 36 11 16 55 04 56 03 34 14 23 46 33 25 22 01 02 24 66 06",
        """
        x0
        p1 26 L
        p2 46 R
        p3 24 L
        p0 45 L
        p1 44 R
        p2 34 R
        p3 25 L
        p0 35 R
        p1 55 R
        p2 56 R
        p3 02 L
        p0 00 L
        p1 04 L
        p2 14 L
        p3 06 R
        p0 05 R
        p1 11 L
        x2
        p3 01 L
        p0 15 R
        p1 16 R
        p2 03 L
        x3
        p0 13 L
        """,
    )
}
