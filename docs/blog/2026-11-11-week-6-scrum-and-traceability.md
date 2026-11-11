# Week 6 — Connecting our sprint to evidence

*Unpublished draft — 11 November 2026. Public access and the real IDE/Git/time demonstration still need verification before publication.*

Our domino project has specifications, diagrams and a small in-memory Cucumber demonstration, but no implemented game yet. This week we started connecting the next pieces of work to a short YouTrack sprint: who is responsible, how much time we expect to need and where the result can be checked.

The useful change is not just having another board. It is being able to follow a task from a plan to an actual change without confusing an estimate, an editor screenshot or a configured report with completed work.

## A one-week plan, not a new project scope

We configured and activated **Elaboration#1, 11–18 November 2026**, on a [new team-editable Scrum board](https://dom1no-dhbw.youtrack.cloud/agiles/204-2/218-2). The old Inception board remains historical: the account doing the setup could not edit it, so we did not overwrite it or treat it as the current sprint.

The sprint contains seven tasks with named owners and estimates totaling **6 h 30 min**. They cover reviewing the current SRS and corrected diagrams, choosing the technology stack, resolving access and Git setup, demonstrating the IDE connection, configuring reports and preparing this update. These are initial estimates for planning, not an agreed team commitment or time already spent.

We kept the RUP fields alongside the sprint. RUP's **phase** tells us where the project is in its lifecycle; its **workflow** describes the activity, such as Requirements or Analysis & Design. The Scrum sprint answers a different question: what work are we trying to finish in this short interval? Reviewing the SRS can therefore be Requirements work in Elaboration and still belong to Elaboration#1.

Our proposed rhythm is simple: agree on a goal and realistic estimates at planning, flag progress and blockers during the week, then review the results and choose an improvement at the end. We have not treated that proposal as evidence that daily meetings or formal Scrum roles are already in place.

## One example: following the friend-request work

[DUC-02](../use-cases/DUC-02-send-friend-request.md) describes finding a player and sending a pending friend request. It already links the intended behavior to scenarios and diagrams. [DOM-35](https://dom1no-dhbw.youtrack.cloud/issue/DOM-35) adds the missing work-tracking demonstration: open that real issue from an IDE task list, run the existing friend-search example, record the actual results and work interval, and push a commit that references the issue.

We connected VS Code using YouTrack for VS Code (`maximtrp.youtrack-ext`) and loaded the real Elaboration#1 issue list. The extension keeps its token in VS Code SecretStorage. That verifies task-list integration, but it has no standalone work timer, and IDE time logging remains to be demonstrated. The Git chain also needs checking: an issue ID in a commit helps identify the task, but the pushed commit must actually appear in YouTrack's VCS changes before we call server-side integration successful.

## What the reports can tell us so far

The [sprint burndown](https://dom1no-dhbw.youtrack.cloud/agiles/204-2/218-2?chart) calculated successfully using estimates and spent time. It began with **390 estimated minutes and zero recorded spent time**. After the board/report configuration task was completed, it showed 330 minutes of remaining estimates and 3 minutes of recorded work. The estimate removed from the backlog is not the same as the time recorded.

The existing time reports distinguish people, RUP workflows, DUC-01-only work, DUC-02-only work and shared work. The SRS and diagram reviews span both detailed use cases, so shared time should remain identifiable rather than being counted in full against each one. We added the existing [cumulative report](https://dom1no-dhbw.youtrack.cloud/reports/cumulativeFlow/563-0) to the dashboard and verified that it aggregates spent-time durations by RUP workflow, not task counts.

The existing In Progress timer also worked: moving DOM-36 from Open to In Progress set its timer-start field. Moving it to Fixed when configuration finished cleared the field and automatically recorded the 3-minute interval. That entry describes agent-led configuration through the API; it is not IDE evidence or a measurement of the whole task's effort. For the IDE demonstration, we will use one logger per interval so the plugin and workflow do not charge for the same work twice.

The [dashboard](https://dom1no-dhbw.youtrack.cloud/dashboard?id=177-4) is shared for reading with All Users and editing with the team, but an anonymous visitor still reaches a login. We therefore cannot yet call it publicly accessible. Fixing that requires checking the guest account and project permissions, not just changing the dashboard's sharing label.

## How this tests our working principles

Our first principle is to **finish a small playable core before adding extras**. This sprint stays with the existing scope and its unresolved decisions instead of adding more game features. Reviewing the SRS before choosing the stack makes that dependency visible.

Our second principle is to **specify behavior once and keep examples consistent**. The corrected PlantUML work still has a review task linked to PR #5; the proposed DUC-02 demonstration must likewise keep its document and scenarios aligned. A task marked complete should point to a reviewed result, not just an edited file.

Our third principle is to **make claims only as strong as the evidence**. The timer transitions produced an actual work item, but the IDE connection is still untested and dashboard sharing has not yet produced anonymous access. AI assisted the setup and this draft; the remaining claims must likewise rest on observed results.

The next step is to finish the access setup and carry out the real IDE-to-issue demonstration. The [project-management guide](../project-management.md) contains the sprint tasks, report links and practical steps. Once the task list, actual time entry and pushed commit are verified, we can replace these remaining plans with the results and review this draft for publication.
