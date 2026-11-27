# DUC-01 acceptance bindings (DOM-49)

`kotlin/dom1no/server/acceptance/DUC01Steps.kt` binds the existing
[`DUC-01-play-domino-match.feature`](../../../docs/features/DUC-01-play-domino-match.feature)
to the real `MatchService` and game engine. `DUC01Games.kt` contains deterministic
decks and legal action histories. Each history is replayed through the service
before the Gherkin action under test; every intermediate transition must be
accepted. The feature and requirements are unchanged.

| Feature scenario | Examples | Bound to production behavior |
| --- | ---: | --- |
| Deal and open a full configured match | 3 | All three variants, dealt hands, opener, stock, first turn |
| Place a matching tile with the selected orientation | 1 | Three-player placement following a real pass |
| Reject a tile that does not match the selected end | 1 | Rejection and unchanged state |
| Reject an out-of-turn placement | 1 | Partnership turn validation and unchanged state |
| Draw until a playable tile is obtained | 1 | Ordered draw and retained turn |
| Exhaust the boneyard without finding a legal placement | 1 | Final draw, then pass |
| End a blocked partnership game after four consecutive passes | 2 | Combined pips, win and tie |
| Resolve a blocked individual game using remaining hand pips | 2 | Two-player win and three-player tie |
| End immediately when a participant places their last tile | 3 | Individual and team wins |
| A placement is known not to have been sent | 1 | **Outstanding:** client/transport failure feedback |
| A response is lost after a placement was sent | 1 | **Outstanding:** WebSocket response and client unconfirmed state |

**DOM-46 owns Cucumber JVM/JUnit Platform integration.** The current Gradle
build has no Cucumber dependencies or runner, so these bindings are deliberately
outside the regular `src/test` source set. They must be connected to DOM-46's
test infrastructure rather than adding a competing Gradle setup here. The
locally available JUnit suite still runs with `./gradlew clean test`.

For validation, a temporary, uncommitted Gradle init script added Cucumber JVM
to the existing `:server` test runtime, included `src/cucumber/kotlin`, pointed
the engine at the original feature and selected every scenario **except** the
two transport/UI paths. Result: **15 Cucumber examples passed; 2 were excluded,
not passed**. This temporary setup is not part of the repository and is not a
replacement for DOM-46. The bindings also use deterministic pre-shuffled decks;
random shuffling itself remains covered by `MatchSetupTest`.
