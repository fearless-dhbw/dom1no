# dom1no — Week 5 Gherkin feature narratives

These feature files describe intended behavior for the two detailed use cases. They are **BDD narratives**, not proof that the app is implemented. A Cucumber.js demonstration executes one scenario in memory; the remaining scenarios are not executable application tests.

| Feature | Detailed use case | Scenarios |
| --- | --- | --- |
| [Play a Domino Match](DUC-01-play-domino-match.feature) | [DUC-01](../use-cases/DUC-01-play-domino-match.md) | Start, legal move, illegal move, win, blocked game |
| [Send Friend Request](DUC-02-send-friend-request.feature) | [DUC-02](../use-cases/DUC-02-send-friend-request.md) | Search success, no match, send, existing friend, accept, decline |

## Scope notes for team review

- **DUC-01:** The `Start a new game` scenario is supporting setup from the team's broader gameplay activity diagram; the detailed use case starts after a match has been created. The four-player setup is taken from that diagram and does not settle the rules profile (`RP-01`). The blocked-game scenario intentionally leaves scoring and tie-breaking undefined until the team approves `RP-01`.
- **DUC-02:** The detailed use case is specifically *sending* a request. Accept/decline scenarios are included because the broader team activity diagram includes them, **not** because acceptance/decline have been reclassified as steps in DUC-02. The existing-friend case and exact feedback wording require confirmation under `DEC-09`.
- Scenario steps describe intended behavior. The in-memory demonstration has six step definitions for one friend-search scenario only; it does not exercise a dom1no application.

## Evidence and review checklist

1. The gameplay and friends screenshots below show Gherkin syntax highlighting in VS Code.
2. The autocomplete screenshot shows a step suggestion from the JavaScript step definitions, not a simulated suggestion.
3. Verify each decision branch against the corresponding activity diagram and resolve any discrepancies with the team.
4. The narratives are linked from their detailed use cases, and the Week 5 blog draft documents AI assistance.
5. The runnable example is an in-memory demonstration only; do not describe it as a test of the application or claim all 11 scenarios are executable.

## Status

Draft for team review. The demonstration below runs only the named scenario.

## Executable demonstration

The repository includes a minimal JavaScript Cucumber.js setup for the existing
`Find a player by username` scenario:

```sh
npm install
npm run test:gherkin-demo
```

This is an **in-memory demonstration**, not a test of a dom1no application:
there is no implemented application or production integration behind these
steps. Only this one scenario has step definitions; the other feature scenarios
remain descriptive narratives. The root `.vscode` settings point the official
Cucumber extension at these feature and step-definition files for editor
completion, and `.vscode/extensions.json` recommends the extension.

## VS Code evidence

- **Gameplay Gherkin highlighting:** [week5-gherkin-gameplay-highlighting.png](../evidence/week5-gherkin-gameplay-highlighting.png)
- **Friends Gherkin highlighting:** [week5-gherkin-friends-highlighting.png](../evidence/week5-gherkin-friends-highlighting.png)
- **Step-definition autocomplete:** [week5-cucumber-step-autocomplete.png](../evidence/week5-cucumber-step-autocomplete.png)
