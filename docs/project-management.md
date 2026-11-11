# Project management and traceability

We use YouTrack to connect planned work, responsibility and evidence. The current iteration is **Elaboration#1, 11–18 November 2026 (UTC)**. RUP describes the kind of work and project phase; Scrum provides a short timebox for planning and reviewing it. A task can therefore belong to the Elaboration phase and the Requirements workflow while also appearing in this sprint.

## Start here

- [Current Scrum board](https://dom1no-dhbw.youtrack.cloud/agiles/204-2/current) — dom1no — Elaboration Scrum
- [Elaboration#1](https://dom1no-dhbw.youtrack.cloud/agiles/204-2/218-2) — fixed sprint link, including its burndown view
- [Dashboard](https://dom1no-dhbw.youtrack.cloud/dashboard?id=177-4) — dom1no — Elaboration#1

The earlier Inception board is historical. We created a team-editable board because the account doing this setup could not edit the old one. The new dashboard is shared for reading with All Users and editing with the dom1no team, but **anonymous access still requires a login**. Sharing a dashboard does not by itself make its issues and reports public; the access check below remains necessary.

## What is planned

The sprint is started: choosing Start sprint removed that button and the board showed 7 days left. Its seven tasks have one responsible assignee each, an estimate, the Elaboration phase and a RUP workflow. These are initial planning estimates, not a claim that the whole team has agreed to this commitment.

| Task | Responsible person | Estimate | RUP workflow |
| --- | --- | --- | --- |
| [DOM-2 — Choose the technology stack](https://dom1no-dhbw.youtrack.cloud/issue/DOM-2) | Darío (`nulkode`) | 1 h 30 min | Analysis & Design |
| [DOM-30 — Review the current SRS](https://dom1no-dhbw.youtrack.cloud/issue/DOM-30) | Sahil | 1 h | Requirements |
| [DOM-27 — Review corrected PlantUML / PR #5](https://dom1no-dhbw.youtrack.cloud/issue/DOM-27) | Mannoubi (`mannoubim26`) | 45 min | Analysis & Design |
| [DOM-34 — Resolve guest access and use-case tag permissions](https://dom1no-dhbw.youtrack.cloud/issue/DOM-34) | Sahil | 45 min | Environment |
| [DOM-35 — Verify VS Code tasks, workflow timing and Git](https://dom1no-dhbw.youtrack.cloud/issue/DOM-35) | Darío | 45 min | Environment |
| [DOM-36 — Configure the board, dashboard and reports](https://dom1no-dhbw.youtrack.cloud/issue/DOM-36) | Darío | 1 h | Environment |
| [DOM-37 — Draft and review the Week 6 blog](https://dom1no-dhbw.youtrack.cloud/issue/DOM-37) | Darío | 45 min | Project Management |

The initial total is **390 minutes (6 h 30 min)**. The configuration and IDE/Git verification tasks are now Fixed. DOM-2 depends on the SRS review; the blog's final evidence review still depends on public access. The current Assignee field supports one owner, not multiple assignees. Time entries can record different contributors.

For use-case reporting, distinguish work exclusive to DUC-01 or DUC-02 from work shared across them. The SRS and diagram reviews are shared work (`UC-MULTI`), not separate hours charged in full to both use cases. The existing DUC-02 tag still needs its owner or an administrator to share tagging permission with the team. The IDE demonstration ultimately used real documentation work on DOM-37, so its interval is not charged to a game use case.

### Proposed working rhythm

At planning, agree on a small goal, check capacity and revise the estimates together. During the week, use short updates to flag progress and blockers and keep issue states current. At the end, review the actual changes and evidence, then choose one improvement for the next iteration. This is a proposed cadence, not a record of daily meetings or Scrum roles already adopted.

## Reading time and progress

Estimation and Spent time are enabled as duration fields. The [sprint burndown](https://dom1no-dhbw.youtrack.cloud/agiles/204-2/218-2?chart) uses both, with the sprint date range configured, and calculated successfully. It began with 390 estimated minutes and no spent time. After DOM-36 was completed, recalculation showed 330 minutes of remaining estimates and 3 minutes recorded by the timer. Completing an estimated task and recording an interval measure different things; neither is evidence that the game is implemented.

Available time reports separate [person](https://dom1no-dhbw.youtrack.cloud/reports/time/227-1), [RUP workflow](https://dom1no-dhbw.youtrack.cloud/reports/time/227-2), [DUC-01-only work](https://dom1no-dhbw.youtrack.cloud/reports/time/227-3), [DUC-02-only work](https://dom1no-dhbw.youtrack.cloud/reports/time/227-4) and [shared work](https://dom1no-dhbw.youtrack.cloud/reports/time/227-5). The dashboard embeds the existing [cumulative report](https://dom1no-dhbw.youtrack.cloud/reports/cumulativeFlow/563-0), which groups RUP workflows and aggregates Spent time as durations; its generic task-count caption should not be read as the unit of this data. Its configured reporting window is 1 October–15 December 2026.

The existing In Progress timer was already operational, so no replacement workflow was attached. Changing DOM-36 from Open to In Progress populated `Timer time`; changing it to Fixed when configuration finished cleared that field and automatically created a 3-minute work item. The entry identifies this as agent-led API configuration, covering only the actual active interval—not IDE work or total human effort. Its duration was not manually entered or adjusted. These observed transitions match the [In Progress Work Timer behavior](https://www.jetbrains.com/help/youtrack/cloud/workflow-work-timer.html). Do not leave a task running after work ends.

## Access and verified IDE/Git integration

Public access still needs a YouTrack administrator. VS Code is connected using **YouTrack for VS Code (`maximtrp.youtrack-ext`, version 2.0.0)**: the actual Elaboration#1 issue list loaded successfully. Its token is stored in VS Code SecretStorage, not repository settings. Git linkage is also verified: the real [Week 6 documentation commit](https://github.com/fearless-dhbw/dom1no/commit/9d2961300416751426b6f175169a3b299824c8e1), pushed to the team repository with `DOM-37` in its message, automatically appeared in that issue's VCS changes.

### 1. Administrator: make the evidence readable

In DOM-34, open **Administration → Access Management → Users → guest** and unban the account if necessary. Assign a read-only role scoped to **DOM only**, with **Read Project Basic**, **Read Issue** and **Read Issue Comment** so visitors can read the linked evidence. YouTrack 2026.2 may require creating this custom role instead of selecting the former Issue Reader role. Check dashboard and report sharing as well, then open the dashboard, sprint, a task and reports in a private browser without logging in. Do not grant guest editing, administration or global project access.

Give the team permission to use the existing DUC-02 tag and apply it to relevant use-case issues. No new GitHub authorization is needed for the tested route: pushing an issue-ID commit to `origin` → `fearless-dhbw/dom1no` produced an actual VCS association. This does not independently verify every fork or branch configuration.

### 2. VS Code: select the real sprint task

Open this repository (`repo/`, not the private parent workspace). In the YouTrack sidebar, select **dom1no → Elaboration Scrum → Elaboration#1**, then DOM-35. The connection and issue-list loading have already been verified. Keep private workspace files, chat and credentials out of evidence captures.

Enter the actual issue ID in VS Code's Source Control commit message. This checkout also has `commit.template` set to [`.gitmessage`](../.gitmessage); in another checkout, run `git config --local commit.template .gitmessage`. The template is a reminder, not automatic attachment to YouTrack.

### 3. IDE-driven time tracking: verified

In VS Code, right-click the real issue and choose **Update → Issue State → In Progress** when work starts. Choose a non-running state when work ends. The extension has no standalone timer; these IDE actions drive YouTrack's existing In Progress workflow, which is the only logger.

This was tested on actual DOM-37 documentation work: the IDE changed it to In Progress at **07:25:29 UTC** and back to Open at **07:26:51 UTC**, leaving final public-evidence review open. YouTrack cleared `Timer time` and automatically created work item **208-31**, recording **1 minute** without a manual duration adjustment. Its description identifies the agent-operated IDE interval, not total human effort. The pushed commit above provides the Git link to the same task.

The [genuine task-list capture](evidence/week6-youtrack-vscode-tasks.png) is cropped to public project content. No private workspace file or token is included. The existing [Week 6 post](https://dom1no.nulkode.dev/2026/11/11/week-6-making-our-project-work-visible/) contains three focused images and the integration results. The remaining access check is anonymous reading, not another IDE installation.
