# Gherkin feature narratives — Week 5

These narratives accompany the two detailed user goals. They specify intended behavior; they are not evidence that the application is implemented.

| Feature | Detailed use case | Paths described |
| --- | --- | --- |
| [Play a Domino Match](DUC-01-play-domino-match.feature) | [DUC-01](../use-cases/DUC-01-play-domino-match.md) | Deal/open, legal/rejected placement, turns, draw/pass, blocked results and ties, empty-hand victory, communication uncertainty |
| [Send Friend Request](DUC-02-send-friend-request.feature) | [DUC-02](../use-cases/DUC-02-send-friend-request.md) | Username/substring search, sender exclusion, send, no results, existing friend, outgoing duplicate, incoming request, self-request, cancellation, unconfirmed save |

Playing starts with a full, configured match: dealing and opening are part of this goal, but joining a queue or room is not. Configurations and outcomes follow the [game rules](../game-rules.md). Accepting/declining a request is supporting friendship behavior, not part of sending one. The use-case pages embed the corresponding activity diagrams and explain remaining recovery and privacy questions.

## Runnable demonstration

```sh
npm ci
npm run test:gherkin-demo
```

Use Node.js 22, 24 or 26+. The command selects only `Find a player by username`: five Gherkin steps against an in-memory fixture, not an application or service. Verification on 9 November passed; Cucumber reports **six steps**, including the setup hook. The mixed-case substring and sender-exclusion scenarios were also run successfully with the same definitions. The remaining scenarios are descriptive; executing all of them against the application is later work.

The search runs directly in Friends, matching the wireframe. VS Code settings connect the features to these demonstration definitions for autocomplete. The [correction example](../blog/2026-11-09-repository-corrections.md#what-the-checks-tell-us) explains what the checks demonstrate.

## IDE evidence

- [Gameplay Gherkin highlighting](../evidence/week6-gherkin-gameplay-highlighting.png)
- [Friends Gherkin highlighting](../evidence/week6-gherkin-friends-highlighting.png)
- [Step-definition autocomplete](../evidence/week6-cucumber-step-autocomplete.png)

The captures demonstrate highlighting and completion on an earlier revision of the feature text, not application behavior.
