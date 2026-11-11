# dom1no — Making the specification describe one game

*Unpublished correction post — 9 November 2026*

Our diagrams, use cases and Gherkin files did not always describe the same behavior. We corrected those disagreements and simplified the reading route from the repository README.

## What changed

The SRS now presents five semester goals, with playing a match and sending a friend request detailed for the current assignment. Computer opponents, profile customization and statistics are deferred.

Players choose two- or three-player individual draw dominoes, or four-player partnership block dominoes. Matchmaking waits for that configuration; it never automatically replaces partnerships with an individual game. Private hosts choose the configuration before players join.

The [game rules](../game-rules.md) cover setup through immediate and blocked endings. Blocked results compare pips, not tile counts: `6|6` has 12 pips, while `0|0` plus `1|1` has only 2. With open ends 2 and 2, neither hand can move, and the second hand wins despite having more tiles.

Friends search now agrees with its wireframe: search directly by a case-insensitive username substring, select a result and send. Results exclude the sender. An existing incoming request is shown with Accept/Decline instead of creating a second request. Sending alone never creates friendship.

## Concrete AI-assisted correction

**Context:** the assistant received the existing SRS, use cases, diagram images, wireframes, feature files, extracted course requirements and an audit. We asked it to improve coherence, use editable PlantUML and ask us before resolving product choices. The assistant and coding subagents edited the artifacts; the team supplied the product decisions.

**Before:** the [original gameplay sequence](../diagrams/gameplay-sequence.png) asked a player to retry an invalid move, then continued to a next-turn message. The feature required the rejected player's turn to remain active.

**After:** the [corrected sequence](../diagrams/gameplay-sequence.svg), with its [PlantUML source](../diagrams/gameplay-sequence.puml), rejects without changing state, permits a retry and advances only after a nonterminal accepted placement. Emptying a hand ends the match immediately. The [activity](../diagrams/gameplay-activity.svg) handles any participant, including an opponent winning while another player waits.

We accepted this correction because it resolves a specific contradiction. Reading the branches checks the intended flow; rendering checks syntax. Neither proves application correctness. We also replaced implementation-like “turn resolved” flags in an initial activity with questions about legal actions and whether the game has ended.

We rejected the assistant's suggestion to offer a smaller game when four players were unavailable. Our final choice was to wait for the selected configuration, which is now consistent across requirements and wireframes.

## Applying our working principles

We adopted the [three working principles](../../README.md#working-principles) on 9 November 2026.

1. **Playable core:** resolve match configurations and endings before promising extra modes or customization.
2. **Consistent behavior:** repair the invalid-turn contradiction and align Friends search across its flow, scenarios and screen.
3. **Evidence-sized claims:** explain what each check demonstrates without treating an in-memory example as an application test.

## What the checks tell us

On 9 November, we rendered and visually inspected the revised diagrams, parsed both feature files and successfully ran the in-memory search examples for exact names, mixed-case substrings and sender exclusion. The [diagram instructions](../diagrams/README.md) and [feature instructions](../features/README.md) explain how to repeat those checks.

The result is a specification whose examples agree more closely, not an implemented game. The next product decisions concern the client platform, recovery after a lost connection, invitations and chat; the [SRS](../SRS.md#7-open-decisions) sets out the choices still needed.
