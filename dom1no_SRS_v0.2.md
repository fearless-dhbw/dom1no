# dom1no — Software Requirements Specification

**Document ID:** DOM-SRS-001  
**Version:** 0.2 (review draft)  
**Date:** 19 October 2026  
**Project:** dom1no — online multiplayer domino game  
**Authors:** Darío, Sahil, Mannoubi  
**Status:** **For team and lecturer review — not baselined**  
**Reference framework:** ISO/IEC/IEEE 29148:2018 (requirements-engineering guidance)

> **Reading this document:** We have written down what dom1no is expected to do based on our current use-case diagrams. Some details, especially the domino rules and the first-release scope, still need a team decision. Those gaps are listed in Section 7 instead of being filled in with guesses. This is a working SRS, not a claim that every feature has already been approved or implemented.

## Document control

| Version | Date | Summary | Status |
| --- | --- | --- | --- |
| 0.1 | 19 Oct 2026 | First draft based on the initial use-case diagrams. | Superseded |
| 0.2 | 19 Oct 2026 | Reworked requirement wording, boundaries, interfaces, verification and traceability; added a rules-definition gate and decision register. | In review |

**Review and approval:** Product scope — pending team agreement; domino rules — pending team agreement; lecturer feedback — pending; baseline approval — **not yet granted**.

**How to read requirements:** A statement using **shall** is a proposed product requirement for the next agreed baseline. An identifier such as `FR-GAME-04` remains stable when we revise the wording. `UC` means the feature appears in a team diagram; `DER` means we have derived a supporting requirement needed for correct or safe behavior. `T`, `D`, `I`, and `A` mean verification by **Test**, **Demonstration**, **Inspection**, and **Analysis**. The IDs and verification methods below are draft assignments for review, not completed test results.

---

## 1. Introduction

### 1.1 Purpose

This document describes the software requirements for **dom1no**. It gives our team one place to check what the game needs to do, what behavior users can expect, and how we plan to verify it. It is also intended for our lecturer and anyone reviewing our analysis, implementation or tests.

We deliberately separate **requirements** (what the software must achieve) from **design decisions** (how we build it). For example, the SRS can require both online players to see the same accepted move without deciding whether our implementation uses WebSockets, HTTP polling or a particular framework.

### 1.2 Product scope

We are building a digital domino game with **single-player** and **online multiplayer** play. Players should be able to play a complete domino match, and online players should see a consistent game state. Our use-case diagrams also cover registration and login, games with friends, player search, friend requests, chat, profiles and settings.

The main challenge for us is not just drawing domino tiles on a screen. The software has to enforce one set of rules, keep track of turns, and handle two players acting through different devices. We want to establish that foundation before treating extra social features as finished.

**Included in the overall product scope described by the diagrams:** gameplay against a computer; online opponent finding; private games and invitation links; account access; friendships; one-to-one friend chat; profile changes and statistics; settings for notifications, sound, language, privacy and password changes.

**Outside the current scope:** paid features, public tournaments, leaderboards, voice or video calls, and native mobile apps *as additional independent products*. These would require a new scope decision; the target client platform itself is still open (see `DEC-03`).

**Release boundary:** "In the overall scope" does **not** mean "promised for the first iteration." Section 7 records which parts still need priority and release decisions.

### 1.3 Intended readers

Our project team will use this document during planning, development and testing. Our lecturer can use it to review the requirements and the reasoning behind them. Future contributors should be able to understand the system without relying on conversations we had during the project.

### 1.4 Terms

| Term | Meaning in this SRS |
| --- | --- |
| **Player** | A person interacting with dom1no. When access rights matter, the text says *authenticated player*. |
| **Computer opponent** | Software that selects moves during single-player play. |
| **Match** | A complete game session under the chosen domino rules. Whether a match contains one or several rounds is not decided. |
| **Round** | A unit of play, if the chosen domino variant uses rounds. |
| **Hand** | The tiles currently available to one player. |
| **Legal move** | A move permitted by the agreed domino rules and the current game state. |
| **Authoritative match state** | The match state against which submitted actions are accepted or rejected. It does not prescribe where that state is hosted. |
| **Private game** | A game joined through an invitation instead of general opponent finding. |
| **TBR** | To Be Resolved before the affected requirement can be baselined or tested. |
| **SRS / UC / RTM** | Software Requirements Specification / Use Case / Requirements Traceability Matrix. |

### 1.5 Standards and sources

1. **ISO/IEC/IEEE 29148:2018**, *Systems and software engineering — Life cycle processes — Requirements engineering*, [ISO catalogue](https://www.iso.org/standard/72089.html). We use its requirements-engineering principles as a reference; this document is **not** a claim of complete standards conformance.
2. **NASA Systems Engineering Handbook, Appendix C**, [How to Write a Good Requirement](https://www.nasa.gov/reference/appendix-c-how-to-write-a-good-requirement/). Used as an open checklist for clear, singular and verifiable requirements.
3. Our team's eight diagrams, listed in Appendix A. These are our primary source for the proposed functional scope.

## 2. System context and users

### 2.1 Product perspective and boundary

The system covers the player-facing application and the software needed to run its domino matches and account/social features. At its boundary, players provide registration details, game actions, invitations, friend requests, messages and settings. The system returns views of the match, results, account data and messages as applicable.

Online matches require communication between participants and an authoritative game state. Persistent accounts, friendships, chat history and settings require storage. **No particular database, server architecture, protocol, programming language or user-interface framework is required by this draft.** Kotlin has been discussed as an option, not agreed as a requirement.

### 2.2 Actors and stakeholders

| Actor / stakeholder | Role or interest |
| --- | --- |
| Player | Starts games, places tiles and views match results. |
| Authenticated player | Uses account-related and social functions that require an identity. |
| Other online player / friend | Participates in online matches, invitations, friend requests and chat. |
| Computer opponent | Takes the opponent's turn in single-player mode. |
| Project team | Defines, builds, checks and maintains the software. |
| Lecturer | Reviews the quality of the requirements and the delivered project. |

**Access decision:** The diagrams do not establish whether single-player play works without an account. We will not treat guest access as a requirement until `DEC-04` is resolved.

### 2.3 Assumptions and dependencies

- Online functions depend on a working connection and the availability of the online game service.
- A complete rules definition is required to judge whether moves, passes and outcomes are correct.
- Authentication and message history depend on persistent data. Exact retention and account-deletion behavior are undecided.
- Our target platform, supported languages, maximum number of players per match and hosting arrangements are TBR.
- The submitted diagrams describe **functions**, not every error path or complete rule of play. Additional derived requirements below make those functions testable.

### 2.4 Main usage flow

A player opens dom1no, chooses a game mode and starts or joins a match. During the match, the software shows the board and available tiles, accepts only legal actions for the active player, and keeps the match state up to date. At the end, it shows the result. Players using accounts can also search for friends, exchange invitations and messages, view their profile and adjust settings.

This is an overview, **not** a substitute for the detailed functional requirements that follow.

## 3. Functional requirements

### 3.1 Required rules definition (baseline gate)

The gameplay requirements below depend on one agreed domino variant. Rather than choose a variant on behalf of the team, we will maintain a **Rules Profile** (`RP-01`). It must define all of the following **before `FR-GAME-04` through `FR-GAME-16` can be baselined or fully tested**:

| Rules-profile field | What we must decide | Status |
| --- | --- | --- |
| RP-01-A | Domino variant and tile set (for example, double-six or another set). | TBR (`DEC-01`) |
| RP-01-B | Permitted number of players and how tiles are dealt. | TBR (`DEC-02`) |
| RP-01-C | Starting player, starting tile and first-turn rules. | TBR |
| RP-01-D | Conditions for a legal placement, including orientation and open ends. | TBR |
| RP-01-E | Drawing, passing or blocked-game behavior when no placement is possible. | TBR |
| RP-01-F | When a round or match ends; how winners, ties and scores are determined. | TBR |
| RP-01-G | Whether a match has multiple rounds or a target score. | TBR |

`RP-01` is a **missing specification to be approved**, not a list of rules that are already true of the game. Every reference to a "legal" move or the "result" below means "according to the approved `RP-01`."

### 3.2 Gameplay — single-player and shared rules

| ID | Requirement statement | Origin | Verify |
| --- | --- | --- | --- |
| **FR-GAME-01** | The system shall allow a player to select single-player mode. | UC: Play Single Player | D |
| **FR-GAME-02** | When single-player mode starts, the system shall create a match with one human player and a computer opponent. | UC: Start Game | T |
| **FR-GAME-03** | When a match starts, the system shall initialize its tiles, hands and first turn according to `RP-01`. | DER: rules | T |
| **FR-GAME-04** | During a match, the system shall display the tiles in the human player's hand. | UC: View Game Board | D |
| **FR-GAME-05** | During a match, the system shall display the tiles already accepted onto the board. | UC: View Game Board | D |
| **FR-GAME-06** | During a match, the system shall identify the player whose turn is active. | DER: turn handling | D |
| **FR-GAME-07** | The system shall allow the active human player to submit a domino placement. | UC: Place Domino Tile | T |
| **FR-GAME-08** | The system shall accept a submitted placement only when it satisfies `RP-01` in the current match state. | DER: legality | T |
| **FR-GAME-09** | When a submitted placement is rejected, the system shall leave the authoritative match state unchanged. | DER: legality | T |
| **FR-GAME-10** | When a placement is accepted, the system shall update the board and the placing player's hand. | DER: state update | T |
| **FR-GAME-11** | Following an accepted move, the system shall set the next active player according to `RP-01`. | DER: turn handling | T |
| **FR-GAME-12** | When the computer opponent is the active player, the system shall select a move permitted by `RP-01`. | UC: Computer Makes Move | T |
| **FR-GAME-13** | When the computer opponent takes its turn, the system shall apply the selected move to the match state. | UC: Computer Makes Move | T |
| **FR-GAME-14** | When no legal placement exists for the active player, the system shall apply the draw, pass or blocked-game rule specified by `RP-01`. | DER: rules | T |
| **FR-GAME-15** | The system shall recognize a finished match using the end conditions specified by `RP-01`. | UC: Finish Game | T |
| **FR-GAME-16** | When a match finishes, the system shall show the outcome calculated under `RP-01`. | UC: Finish Game | T |

### 3.3 Online play and match consistency

| ID | Requirement statement | Origin | Verify |
| --- | --- | --- | --- |
| **FR-ONL-01** | The system shall allow a player to choose online play. | UC: Play Online | D |
| **FR-ONL-02** | When a player requests an online opponent, the system shall offer a way to join a match with another player. | UC: Find Opponent | D |
| **FR-ONL-03** | The system shall identify each online match separately from other concurrent matches. | DER: state isolation | T |
| **FR-ONL-04** | The system shall accept a match action only from a participant in that match. | DER: authorization | T |
| **FR-ONL-05** | The system shall reject a placement from a participant who is not the active player. | DER: turn handling | T |
| **FR-ONL-06** | The system shall evaluate each submitted online move against the authoritative match state before accepting it. | DER: consistency | T |
| **FR-ONL-07** | When an online move is accepted, the system shall make the resulting match state available to both participants. | UC: Play Online | T |
| **FR-ONL-08** | When an online move is rejected, the system shall inform its sender that the move was not accepted. | DER: error feedback | T |
| **FR-ONL-09** | The system shall prevent two conflicting move requests from both becoming accepted transitions from the same match state. | DER: concurrency | T |
| **FR-ONL-10** | When an online action cannot be delivered, the system shall not present that action as an accepted move. | DER: connectivity | T |

**Still open:** The matchmaking method, waiting-room flow, disconnect/rejoin behavior, match cancellation and abandoned-match outcome are not specified by the diagrams. They appear in `DEC-05` and `DEC-06`, not as invented finished requirements.

### 3.4 Registration, login and account access

| ID | Requirement statement | Origin | Verify |
| --- | --- | --- | --- |
| **FR-AUTH-01** | The system shall provide a registration form that accepts a username, email address and password. | UC: Register | D |
| **FR-AUTH-02** | The system shall reject a registration submission when any required field is missing. | UC: Validate Credentials | T |
| **FR-AUTH-03** | The system shall reject registration when the requested username is already assigned to an account. | DER: account identity | T |
| **FR-AUTH-04** | The system shall create an account only after the registration data passes the agreed validation rules. | UC: Register | T |
| **FR-AUTH-05** | The system shall provide login using the account credentials selected by the team. | UC: Login | D |
| **FR-AUTH-06** | The system shall deny access when submitted login credentials are invalid. | UC: Validate Credentials | T |
| **FR-AUTH-07** | The system shall provide a logout action for an authenticated player. | UC: Logout | D |
| **FR-AUTH-08** | Following logout, the system shall prevent further use of the ended session for authenticated actions. | DER: session control | T |

**Validation still to specify:** Username length/character policy, email validation, password policy, email uniqueness, login identifier (email versus username), account recovery and email verification. These are in `DEC-07`. Until then, `FR-AUTH-04` and `FR-AUTH-05` remain incomplete for full acceptance testing.

### 3.5 Private games and invitations

| ID | Requirement statement | Origin | Verify |
| --- | --- | --- | --- |
| **FR-INV-01** | The system shall allow a player to create a private game. | UC: Create Private Game | D |
| **FR-INV-02** | For each created private game, the system shall provide an invitation link or equivalent join identifier. | UC: Generate Invitation Link | T |
| **FR-INV-03** | The system shall allow the host to obtain the invitation for sharing. | UC: Share Game Link | D |
| **FR-INV-04** | When a player opens a valid invitation, the system shall identify the associated private game. | UC: Open Invitation Link | T |
| **FR-INV-05** | The system shall allow an eligible invited player to join the associated game room. | UC: Enter Game Room | T |
| **FR-INV-06** | The system shall reject an invitation that does not identify an available private game. | DER: invalid invitation | T |
| **FR-INV-07** | When joining a private game is rejected, the system shall display the reason to the joining player. | DER: error feedback | D |

**Still open:** Invite lifetime, number of uses, privacy of the link, whether non-friends may join, what counts as an "eligible" joiner, and room capacity (`DEC-08`). We should not silently assume invitations expire, that a link is single-use, or that only friends can open one.

### 3.6 Finding and managing friends

| ID | Requirement statement | Origin | Verify |
| --- | --- | --- | --- |
| **FR-FRD-01** | The system shall provide a way to search for another player by the search identifier the team selects. | UC: Search Player | D |
| **FR-FRD-02** | The system shall display a player's existing friends list. | UC: View Friends List | D |
| **FR-FRD-03** | The system shall allow an authenticated player to send a friend request to another player. | UC: Send Friend Request | T |
| **FR-FRD-04** | The system shall present a received friend request to its intended recipient. | DER: request receipt | D |
| **FR-FRD-05** | The system shall allow the recipient to accept a pending friend request. | UC: Accept Friend Request | T |
| **FR-FRD-06** | After acceptance, the system shall record a friendship between the two accounts. | DER: friendship state | T |
| **FR-FRD-07** | The system shall show accepted friends in each participant's friends list. | UC: View Friends List | T |
| **FR-FRD-08** | The system shall allow an authenticated player to remove an existing friend. | UC: Remove Friend | T |
| **FR-FRD-09** | When a player removes a friend, the system shall remove that friendship from both participants' friends lists. | DER: friendship state | T |

**Still open:** Search fields, blocked users, duplicate and incoming request handling, declining or cancelling requests, and whether profiles are searchable by everyone (`DEC-09`).

### 3.7 Chat with a friend

| ID | Requirement statement | Origin | Verify |
| --- | --- | --- | --- |
| **FR-CHAT-01** | The system shall let an authenticated player open a conversation with a friend. | UC: Open Chat | D |
| **FR-CHAT-02** | The system shall allow a player to submit a text message to the friend in that conversation. | UC: Send Message | T |
| **FR-CHAT-03** | The system shall associate each accepted message with its sender and conversation. | DER: message identity | T |
| **FR-CHAT-04** | The system shall make an accepted message available to the intended recipient. | UC: Receive Message | T |
| **FR-CHAT-05** | The system shall display the available message history when a participant opens the conversation. | UC: View Chat History | T |
| **FR-CHAT-06** | The system shall prevent a player outside a conversation from reading its messages through the application. | DER: access control | T |

**Still open:** Character limit, deletion and retention, unread indicators, whether chat is live or refreshed on demand, and what happens to history after unfriending (`DEC-10`).

### 3.8 Profile

| ID | Requirement statement | Origin | Verify |
| --- | --- | --- | --- |
| **FR-PRO-01** | The system shall allow an authenticated player to view their profile. | UC: View Profile | D |
| **FR-PRO-02** | The system shall allow an authenticated player to submit a replacement profile picture. | UC: Change Profile Picture | T |
| **FR-PRO-03** | The system shall allow an authenticated player to request a username change. | UC: Update Username | T |
| **FR-PRO-04** | The system shall reject a username change that conflicts with the agreed username rules. | DER: account identity | T |
| **FR-PRO-05** | The system shall display the game statistics defined for a player's profile. | UC: View Statistics | D |

**Still open:** Supported image types and size, how images are moderated or removed, username reuse, which statistics are recorded and who is allowed to see them (`DEC-11`).

### 3.9 Settings

| ID | Requirement statement | Origin | Verify |
| --- | --- | --- | --- |
| **FR-SET-01** | The system shall let an authenticated player open their settings. | UC: Manage Settings | D |
| **FR-SET-02** | The system shall provide a control for the supported notification preferences. | UC: Change Notifications | D |
| **FR-SET-03** | The system shall provide a control for game sound preferences. | UC: Change Sound | D |
| **FR-SET-04** | The system shall allow a player to select from the supported interface languages. | UC: Change Language | T |
| **FR-SET-05** | The system shall provide controls for the privacy options agreed by the team. | UC: Change Privacy | D |
| **FR-SET-06** | The system shall allow an authenticated player to request a password change. | UC: Change Password | T |
| **FR-SET-07** | The system shall require verification of the player's existing credentials or an approved recovery method before changing the password. | DER: account security | T |
| **FR-SET-08** | When a player's setting is saved, the system shall restore the saved value in their next authenticated session. | DER: persistence | T |

**Still open:** Which notifications exist, whether sound means effects and/or music, supported languages, specific privacy choices and password-change policy (`DEC-12`).

## 4. Data and external interface requirements

### 4.1 Logical data requirements

The following describes the information the software must handle, **not** a database schema. Field names, storage products and retention periods are design/decision work.

| Data group | Information needed | Related requirements |
| --- | --- | --- |
| Account | Account identity and credential verification data | `FR-AUTH-*`, `FR-PRO-*`, `FR-SET-06–07` |
| Match | Match identity, participants, current tiles/board, turn, state, outcome | `FR-GAME-*`, `FR-ONL-*` |
| Private game | Game identity and a join reference | `FR-INV-*` |
| Friendship | The two accounts and the relationship/request state | `FR-FRD-*` |
| Message | Conversation, sender, content and order | `FR-CHAT-*` |
| Preferences | Saved setting values for an account | `FR-SET-*` |
| Statistics | Agreed match-result measurements for each player | `FR-PRO-05` |

The team must define whether histories and statistics survive account deletion, whether players may export their data, and whether old match state is retained. These are unresolved data-lifecycle questions, not silently approved requirements.

### 4.2 User interface

The product needs user-facing entry points for authentication; choosing a mode; viewing/playing a match; opponent finding; private invitations; friends and chat; profiles; and settings. The screens may be combined if the final design supports every accepted function.

| ID | Requirement statement | Origin | Verify |
| --- | --- | --- | --- |
| **IR-UI-01** | When a match is active, the interface shall distinguish the current player's hand from domino tiles already placed on the board. | DER: board interpretation | I |
| **IR-UI-02** | Following a rejected game action, the interface shall tell the acting player that the action was rejected. | DER: error feedback | D |
| **IR-UI-03** | When a match finishes, the interface shall provide a visible match-ended state. | UC: Finish Game | D |
| **IR-UI-04** | The interface shall identify whether an online game is waiting for another player or is in progress. | DER: online status | D |

The supported screen sizes, devices and accessibility target are TBR (`DEC-03`, `DEC-13`). Mockups and screen layout details belong in the interface design, not in place of these requirements.

### 4.3 Online software interface

At the logical level, online play requires a client action (including the match and acting player), a decision to accept or reject that action, and a resulting match state for participants. The protocol, event format, error codes and hosting model are not chosen.

| ID | Requirement statement | Origin | Verify |
| --- | --- | --- | --- |
| **IR-NET-01** | Each online placement request shall be associated with one identified match. | DER: routing | T |
| **IR-NET-02** | The system shall return an acceptance or rejection outcome for a submitted online placement. | DER: acknowledgement | T |
| **IR-NET-03** | The system shall distinguish successive accepted states of the same online match so an out-of-date move cannot overwrite a newer state. | DER: ordering | T |

### 4.4 Hardware and third-party interfaces

No special hardware is identified in the use-case diagrams. A compatible client device and network connectivity are needed for online play. Third-party identity, messaging, analytics or hosting services have **not** been selected and must not be assumed here.

## 5. Quality requirements

Quality requirements also need acceptance conditions. The values labelled **proposed target** below are starting points for team discussion, not measurements or promises already validated. They can only enter an approved baseline together with their test environment.

### 5.1 Correctness and reliability

| ID | Requirement statement | Verification |
| --- | --- | --- |
| **NFR-COR-01** | An online placement rejected under `RP-01` shall produce no change to the authoritative match state. | Automated test using illegal placements |
| **NFR-COR-02** | A match shall not apply a placement request from a non-active participant. | Automated turn-order test |
| **NFR-COR-03** | After two participants receive the same accepted match-state revision, their board representation and active-player indicator shall agree with that revision. | Two-client integration test |
| **NFR-REL-01** | If an online move cannot be submitted, the client shall indicate that the move has not been confirmed. | Connection-failure test |

These checks concern observable behavior. They do not require a specific networking library or architecture.

### 5.2 Performance — proposed acceptance targets

| ID | Proposed measurable target | Test setup / status |
| --- | --- | --- |
| **NFR-PER-01** | For at least **95 of 100** accepted online placements, the other connected participant shall display the resulting board state within **2 seconds** of acceptance. | **Proposed target.** Verify across multiple games, with two clients on the same defined test network; record device specifications, latency and service environment before approval. |
| **NFR-PER-02** | For at least **95 of 100** local board interactions, the interface shall show an acknowledgement within **500 ms** of the input event. | **Proposed target.** Use the chosen reference device; measure with automation or screen/event timestamps after `DEC-03`. |

We have deliberately left out a simultaneous-user capacity target: the team has not chosen the deployment environment or expected load (`DEC-14`). A made-up capacity number would not make the SRS better.

### 5.3 Security and privacy

| ID | Requirement statement | Verification |
| --- | --- | --- |
| **NFR-SEC-01** | Stored account passwords shall be represented using a password-hashing method intended for password storage, not in plaintext or by reversible encryption. | Code/configuration inspection and storage check |
| **NFR-SEC-02** | A user shall not be able to modify another account's profile or settings without authorization. | Unauthorized request test |
| **NFR-SEC-03** | A user shall not be able to read private conversation content unless they are a participant in that conversation. | Unauthorized request test |
| **NFR-SEC-04** | Online move acceptance shall not depend only on a player's claim that a move is legal or that it is their turn. | Invalid-client-request test |
| **NFR-SEC-05** | When account credentials or private messages travel over a public network, their transport shall use encryption in transit. | Deployment configuration inspection |

Before making the service publicly available, we also need to decide account deletion, personal-data retention and a privacy notice appropriate to the actual deployment. Those are release decisions recorded as `DEC-15`, not a claim that the draft is already legally complete.

### 5.4 Usability and accessibility

| ID | Requirement statement | Verification |
| --- | --- | --- |
| **NFR-USE-01** | The interface shall show which participant is expected to act during an active match. | Demonstration in both modes |
| **NFR-USE-02** | The interface shall communicate whether a submitted move was accepted or rejected. | Demonstration of both outcomes |
| **NFR-ACC-01** | Turn ownership and move rejection shall not be conveyed by color alone. | Visual inspection |

A full usability-study success rate and a formal accessibility level would require defined user tasks and a chosen target platform. They are therefore not presented here as confirmed figures.

### 5.5 Maintainability and testability

| ID | Requirement statement | Verification |
| --- | --- | --- |
| **NFR-TEST-01** | The chosen rules implementation shall be verifiable through automated tests for legal moves, illegal moves, turn transitions and end conditions without manual UI interaction. | Test execution and inspection |
| **NFR-TEST-02** | The system shall provide a reproducible way to initialize a match state for rules testing. | Automated test demonstration |

Version-control practices, issue-tracker configuration and coding conventions belong to the **project plan** rather than to product behavior, so they are not stated as software requirements in this SRS.

## 6. Verification and traceability

### 6.1 How requirements will be checked

The following verification methods are used in Section 3:

| Code | Meaning | Example |
| --- | --- | --- |
| **T** | Test: compare actual behavior with an expected result. | An out-of-turn placement is rejected. |
| **D** | Demonstration: show behavior to a reviewer. | A user opens a profile. |
| **I** | Inspection: examine a document, screen or implementation. | The interface distinguishes a hand from the board. |
| **A** | Analysis: reason from evidence or measurements. | Review measured performance results. |

**Verification is planned, not claimed complete.** Test IDs below are placeholders for tests we will write and link to implementation issues during development.

### 6.2 Requirements traceability matrix

Each source below is one of our team's diagrams (not one of the previous-year examples). Derived requirements are linked to the feature whose reliable implementation requires them.

| Source / use-case area | Requirements | Planned verification set |
| --- | --- | --- |
| Gameplay Use Cases: single-player, board, placement, computer turn, finish | `FR-GAME-01`–`FR-GAME-16` | `TC-GAME-*` |
| Gameplay Use Cases: online opponent | `FR-ONL-01`–`FR-ONL-10`; `IR-NET-01`–`IR-NET-03` | `TC-ONL-*` |
| Authentication Use Case | `FR-AUTH-01`–`FR-AUTH-08` | `TC-AUTH-*` |
| Friend Game System Use Case | `FR-INV-01`–`FR-INV-07` | `TC-INV-*` |
| Friends and Player Search Use Case | `FR-FRD-01`–`FR-FRD-09` | `TC-FRD-*` |
| Chat with Friend Use Case | `FR-CHAT-01`–`FR-CHAT-06` | `TC-CHAT-*` |
| Manage Profile Use Case | `FR-PRO-01`–`FR-PRO-05` | `TC-PRO-*` |
| Manage Settings Use Case | `FR-SET-01`–`FR-SET-08` | `TC-SET-*` |
| Cross-cutting product quality and UI | `IR-UI-*`; `NFR-*` | `TC-QUAL-*` |

**General Use Case** provides the overview of these same areas and does not create a second, conflicting set of requirements.

### 6.3 Worked acceptance tests

These are examples of how we will move from requirement wording to concrete verification. The `RP-01` examples only become executable once we have written down the chosen domino rules.

**TC-GAME-01 — Illegal tile placement**  
**Covers:** `FR-GAME-08`, `FR-GAME-09`, `NFR-COR-01`.  
**Given:** An active match state defined under `RP-01`, with a tile/placement that is illegal in that state.  
**When:** The active player submits that placement.  
**Then:** The request is rejected and the authoritative match state is unchanged.

**TC-ONL-01 — Move submitted out of turn**  
**Covers:** `FR-ONL-05`, `NFR-COR-02`.  
**Given:** Player A and Player B are in the same match and it is Player A's turn.  
**When:** Player B submits a placement.  
**Then:** The system rejects the request and leaves the match state unchanged.

**TC-ONL-02 — Accepted move reaches both players**  
**Covers:** `FR-ONL-07`, `NFR-COR-03`.  
**Given:** Two connected players see the same starting match-state revision.  
**When:** The active player submits a legal move and it is accepted.  
**Then:** Both players receive the resulting revision and display the board and active turn specified by it.

**TC-AUTH-01 — Incorrect credentials**  
**Covers:** `FR-AUTH-06`.  
**Given:** A registered account exists.  
**When:** Someone submits an incorrect password for that account.  
**Then:** No authenticated session is granted.

**TC-INV-01 — Invalid invitation**  
**Covers:** `FR-INV-06`, `FR-INV-07`.  
**Given:** A join link that points to no available private game.  
**When:** A player opens it.  
**Then:** The player is not added to a game room and sees the reason joining failed.

**TC-FRD-01 — Friend acceptance**  
**Covers:** `FR-FRD-03`–`FR-FRD-07`.  
**Given:** Two authenticated accounts that are not friends.  
**When:** One sends a request and the other accepts it.  
**Then:** The friendship is recorded and appears in each player's friends list.

**TC-CHAT-01 — Unauthorized message access**  
**Covers:** `FR-CHAT-06`, `NFR-SEC-03`.  
**Given:** Two accounts have a private conversation and a third account is not a participant.  
**When:** The third account attempts to retrieve that conversation.  
**Then:** The system denies access to its content.

### 6.4 Change control

For a change to be accepted into the SRS, we will record (1) the affected requirement ID, (2) the reason, (3) the diagrams or decisions it changes, and (4) the test cases that must be revised. We will then update the version history. An approved baseline should only be declared after the outstanding decisions that affect that release are resolved.

## 7. Decisions before baselining

This is the part we need to work through as a team. The SRS should make missing decisions visible; it should not pretend we have already made them.

| ID | Decision we still need | Why it matters | Status |
| --- | --- | --- | --- |
| **DEC-01** | Which domino variant and complete rules profile? | Legal moves, winner calculation and almost all gameplay tests depend on this. | **Blocker** |
| **DEC-02** | How many people can play in one online match and in single-player? | Affects hand sizes, game flow and capacity. | **Blocker** |
| **DEC-03** | Which client platform and supported devices? | Determines UI, performance test setup and accessibility targets. | Open |
| **DEC-04** | Can a guest play single-player? Which features require login? | Affects entry flow and account requirements. | Open |
| **DEC-05** | How are online opponents found and when does a match start? | `FR-ONL-02` is not fully testable yet. | Open |
| **DEC-06** | What happens on disconnect, rejoin, quit or abandonment? | Needed for complete online-match behavior. | **Blocker for online release** |
| **DEC-07** | Registration/login identifiers and validation policies; recovery? | Needed for complete authentication acceptance tests. | Open |
| **DEC-08** | Private-room capacity and invitation validity/permissions? | Needed to define eligible invitation use. | Open |
| **DEC-09** | Player search visibility and friend-request edge cases? | Needed for complete friends behavior. | Open |
| **DEC-10** | Message length, delivery expectations and retention? | Needed for chat tests and data handling. | Open |
| **DEC-11** | Which statistics and profile image/username restrictions? | Needed for profile tests and storage. | Open |
| **DEC-12** | Which languages, notifications and privacy options? | Needed for settings acceptance tests. | Open |
| **DEC-13** | Which screen sizes and accessibility standard or target? | Needed for verifiable interface-quality requirements. | Open |
| **DEC-14** | Hosting/test environment and expected simultaneous load? | Needed for measurable performance/capacity requirements. | Open |
| **DEC-15** | Public release or classroom demo only? What data lifecycle applies? | Affects privacy, retention, deployment and security verification. | Open |
| **DEC-16** | Which feature groups are mandatory in the first delivery? | Prevents overall scope from becoming an unrealistic single milestone. | **Blocker for release plan** |

### Proposed implementation order — **not** an approved commitment

Our suggestion is to tackle the riskiest part first: (1) agree on the rules and make a complete local match work; (2) verify two-player online synchronization; (3) add a simple way to enter a match; (4) build out accounts, friends, invitations, chat, profiles and settings in agreed iterations. This sequence is a **planning proposal**, not a statement that the later features have been cancelled.

## Appendix A — Use-case diagram register

The source files are provided separately in the project materials. When we add this SRS to GitHub, we should keep the diagrams alongside it so the references remain accessible.

| Diagram ID | Team diagram | Requirement area |
| --- | --- | --- |
| `UC-00` | [General Use Case.png](diagrams/General%20Use%20Case.png) | Overall system boundary |
| `UC-01` | [Gameplay Use Cases.png](diagrams/Gameplay%20Use%20Cases.png) | Gameplay and online play |
| `UC-02` | [Authentication Use Case.png](diagrams/Authentication%20Use%20Case.png) | Registration, login and logout |
| `UC-03` | [Friend Game System Use Case.png](diagrams/Friend%20Game%20System%20Use%20Case.png) | Private games and invitations |
| `UC-04` | [Friends and Player Search Use Case.png](diagrams/Friends%20and%20Player%20Search%20Use%20Case.png) | Friends and search |
| `UC-05` | [Chat with Friend Use Case.png](diagrams/Chat%20with%20Friend%20Use%20Case.png) | Chat |
| `UC-06` | [Manage Profile Use Case.png](diagrams/Manage%20Profile%20Use%20Case.png) | Profile |
| `UC-07` | [Manage Settings Use Case.png](diagrams/Manage%20Settings%20Use%20Case.png) | Settings |

## Appendix B — Document review checklist

Before we mark a version **approved**, we will check that:

- [ ] `RP-01` defines the selected domino game completely enough to write expected outcomes.
- [ ] The first-delivery boundary and target client platform have been agreed.
- [ ] Every "shall" statement describes one observable or verifiable obligation.
- [ ] Required values (limits, languages, permissions, timeouts) are defined or clearly identified as unresolved.
- [ ] Every feature group can be traced to a use case, stakeholder need or documented derived requirement.
- [ ] Each accepted requirement has a verification method and, eventually, a linked test.
- [ ] Quality targets have realistic, repeatable test conditions.
- [ ] The team has reviewed contradictory assumptions and the lecturer's assignment-specific instructions.
- [ ] The document version, approval and change history are up to date.

**End of SRS — version 0.2, review draft.**
