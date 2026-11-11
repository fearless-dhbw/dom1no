# dom1no

An online domino game for two or three individual players, or four players in partnerships. Players choose a configuration and wait for that match; the system never silently switches variants.

This repository contains the Software Engineering I specification and design work, plus a small Cucumber editor/framework demonstration. **There is no implemented game yet.**

## Understand the project

1. [SRS: purpose, five semester goals and requirements](docs/SRS.md)
2. [Domino rules: setup, drawing/passing and how a match ends](docs/game-rules.md)
3. Detailed use cases: [Play a match](docs/use-cases/DUC-01-play-domino-match.md) and [Send a friend request](docs/use-cases/DUC-02-send-friend-request.md), with activities, wireframes and scenarios
4. [Gherkin scenarios and IDE evidence](docs/features/README.md)

Only two of the five semester goals are detailed now. The [SRS open decisions](docs/SRS.md#7-open-decisions) distinguish remaining proposals from agreed behavior; implementing the application and executing its scenarios are later milestones.

## Run the demonstration

Use Node.js 22, 24 or 26+ and npm:

```sh
npm ci
npm run test:gherkin-demo
```

This executes one friend-search scenario against in-memory data, not an application. The [feature README](docs/features/README.md) explains the evidence and remaining descriptive scenarios.

## Edit diagrams

UML sources and their committed SVG renders are in [docs/diagrams](docs/diagrams/README.md). Update the `.puml` source and render the SVG together; readers do not need PlantUML installed. The [gameplay](docs/mockups/gameplay.svg) and [friends](docs/mockups/friends.svg) wireframes are themselves editable SVG sources: open them in a browser and edit them as XML or in an SVG editor. Their labeled panels are examples, not a complete match transcript.

## Working principles

Adopted on 9 November 2026.

1. **Finish a small playable core before adding extras.** Make one complete game work before adding computer opponents, profile customization or statistics.
2. **Specify behavior once and keep examples consistent.** The rules explain behavior; diagrams, wireframes and scenarios illustrate it. Update affected examples when a rule changes.
3. **Make claims only as strong as the evidence.** A rendered diagram is not a working game, and an in-memory demonstration is not an application test. Report concrete changes and observed results.

Each new blog update should briefly explain what supported or challenged these principles, linking the relevant work rather than repeating their definitions.

## Reporting and evidence

- [Project management](docs/project-management.md) — current sprint, reports and remaining access/IDE setup
- [Team blog](https://dom1no.nulkode.dev) — published progress and peer comments
- [Unpublished correction post](docs/blog/2026-11-09-repository-corrections.md) — concrete AI before/after example and verification results
- [Week 6 draft](docs/blog/2026-11-11-week-6-scrum-and-traceability.md) — sprint planning and traceability; not yet published
