# dom1no — From Use Cases to Gherkin Feature Files

*Week 5 project update — 3 November 2026*

This week, we took two of our existing use cases and turned them into Gherkin feature files. The idea was to describe what players should be able to do in a way that we could eventually turn into tests.

We focused on **Play a Domino Match** and **Send a Friend Request**. Between them, we now have 11 scenarios covering things like making a valid or invalid move, ending a game, finding another player, and accepting or declining a friend request. We compared the scenarios with our activity diagrams to make sure we hadn't only written down the happy paths.

We also tried out **Cucumber.js** in VS Code. Setting up step-definition autocomplete was a little more fiddly than expected, but we eventually got it working with the official Cucumber extension. We managed to run one small friend-search example successfully: **1 scenario, 7 steps passed**. This uses in-memory test data, so it demonstrates how Cucumber works rather than testing a finished dom1no application.

### How we used AI

We used ChatGPT to review the Gherkin wording, compare the scenarios with the activity diagrams, and spot missing alternative paths. We then used an AI coding agent in VS Code to help set up Cucumber.js, write the demonstration step definitions, and troubleshoot the editor configuration. We reviewed the results, ran the test, and captured real screenshots of the IDE ourselves. AI helped speed up the setup, but we still needed to understand what was being tested and check that the results were genuine.

Next, we want to agree on the remaining game-rule details and gradually connect these scenarios to actual application behavior.

**Project files:** [Gherkin features](https://github.com/fearless-dhbw/dom1no/tree/main/docs/features) · [Detailed use cases](https://github.com/fearless-dhbw/dom1no/tree/main/docs/use-cases)
