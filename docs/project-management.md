# Working with YouTrack

YouTrack holds our tasks, responsibility and recorded effort; this repository holds the specification, designs and code. Use the issue to connect the two.

## Find the current work

- [Current Scrum board](https://dom1no-dhbw.youtrack.cloud/agiles/204-2/current) — find tasks, owners and progress.
- [Elaboration#1, 11–18 November 2026 (UTC)](https://dom1no-dhbw.youtrack.cloud/agiles/204-2/218-2) — review and integrate the specification, choose a technology stack, and check reporting and IDE/Git integration. The goal is a reviewed basis for implementation, not a playable game.
- [Dashboard](https://dom1no-dhbw.youtrack.cloud/dashboard?id=177-4) — see the sprint and reports together.
- [Repository guide](../README.md#understand-the-project) — find the SRS, detailed use cases, diagrams, wireframes and scenarios.

The board is the current task list. For example, the technology decision ([DOM-2](https://dom1no-dhbw.youtrack.cloud/issue/DOM-2)) depends on the SRS review ([DOM-30](https://dom1no-dhbw.youtrack.cloud/issue/DOM-30)); coordinate with its owner before building on that result. Public views are read-only; sign in with your own team account to participate. Ask the team for DOM editing access if needed.

## Take a task through to review

1. **Coordinate before starting.** Read the issue, linked material and comments. Check with its assignee before taking over or duplicating work. Keep one responsible **Assignee**, a realistic **Estimation**, and the relevant sprint on the issue.
2. **Classify the work.** **Phase** identifies the lifecycle stage, currently Elaboration; **RUP Workflow** identifies the kind of work, such as Requirements or Analysis & Design. Use the existing `UC-DUC-01` or `UC-DUC-02` tags for use-case work. For work shared across both, retain both tags and `UC-MULTI`; record the effort once, not in full against each use case. DOM-30 is an example. Do not assign unrelated setup or reporting work to a game use case.
3. **Time actual work.** Set the issue to **In Progress** when you start. Move it back to **Open** when pausing, or **Fixed** when finished. Leaving In Progress stops YouTrack's timer and creates a work entry automatically. Do not leave it running during breaks or add the same interval manually.
4. **Connect the change.** Include the real issue ID in the commit message, for example `DOM-30: clarify match setup`. Once pushed to the connected repository, the commit should appear in the issue's **VCS changes**. Link the actual commit or pull request in the issue when sharing the result. [`.gitmessage`](../.gitmessage) is a reminder, not automatic linkage; enable it in a new checkout with `git config --local commit.template .gitmessage`.
5. **Share the result or blocker.** Comment on what changed, what you checked and what still needs attention, with links to the relevant material. For a review, record findings and unresolved questions so the owner can act. If blocked, stop the timer and explain what is needed to continue.

## Work from VS Code

Open the team repository in VS Code. Install **YouTrack for VS Code** (`maximtrp.youtrack-ext`), then choose **YouTrack: Add a Server**. Enter `https://dom1no-dhbw.youtrack.cloud` and your own [YouTrack token](https://www.jetbrains.com/help/youtrack/cloud/Manage-Permanent-Token.html) in the masked API-key prompt. The extension stores it in VS Code SecretStorage; do not put it in repository settings, chat or screenshots.

Select the server, **dom1no — Elaboration Scrum**, the **dom1no** project and **Elaboration#1**, then your actual task. Right-click it and use **Update → Issue State → In Progress** to start work; choose Open or Fixed to stop. These state changes use the same YouTrack timer as the browser, not a separate IDE timer.

## Read effort and progress

- [Sprint burndown](https://dom1no-dhbw.youtrack.cloud/agiles/204-2/218-2?chart) — remaining estimates and recorded time. Finishing an estimated task is not the same as spending that estimated duration.
- Time reports by [person](https://dom1no-dhbw.youtrack.cloud/reports/time/227-1), [RUP workflow](https://dom1no-dhbw.youtrack.cloud/reports/time/227-2), [DUC-01-only work](https://dom1no-dhbw.youtrack.cloud/reports/time/227-3), [DUC-02-only work](https://dom1no-dhbw.youtrack.cloud/reports/time/227-4) and [shared work](https://dom1no-dhbw.youtrack.cloud/reports/time/227-5).
- [Cumulative report](https://dom1no-dhbw.youtrack.cloud/reports/cumulativeFlow/563-0) — spent time grouped by RUP workflow, covering 1 October–15 December 2026.

The [Week 6 blog post](https://dom1no.nulkode.dev/2026/11/11/week-6-making-our-project-work-visible/) contains the demonstration evidence for planning, reports, IDE timing and Git integration.
