# dom1no — From Requirements to Activity Diagrams and UI Mockups

*Weekly project update — draft for WordPress (week ending 25 October 2026)*

This week, our work on dom1no focused on making the game's behavior easier to understand before we start implementing it. Instead of only listing features, we worked through how a player actually moves through the system and how the application should react.

## Mapping the flow

We created activity diagrams for **gameplay** and **friends management**. For gameplay, we looked at turns, selecting a domino, checking whether a move is valid, and reaching the result screen. The friends diagram explores how players search for and interact with each other.

One thing we noticed is that a general activity diagram and a detailed use case are not quite the same thing. The friends diagram, for example, includes sending, accepting, and removing friendships. In our detailed documentation, we've narrowed this down to **Send a Friend Request**, because that is one clear goal for a player.

## Thinking about system interactions

We also produced sequence diagrams. While the activity diagrams helped us think about the flow from a user's point of view, the sequence diagrams helped us start thinking about the messages exchanged between the interface and the system.

## Giving the game a visual shape

Alongside the diagrams, we worked on UI mockups in Figma. We now have screens showing the start of a game, a player's turn, a selected tile, invalid-move feedback, waiting for another player, and match results. We also mocked up the friends screen and removing a friend.

The mockups are still design proposals, but they're useful because they make our questions much more concrete: What should players see when they cannot make a move? How do we communicate whose turn it is? What happens when the system rejects an action?

## Keeping the SRS aligned

We linked the detailed use cases back to the requirements in our existing SRS. We also kept a few decisions explicitly open instead of guessing, especially the exact domino variant and draw/pass rules, and how duplicate friend requests should work. That gives us a clearer basis for the next team discussion.

## What's next?

Next, we want to review the diagrams and use cases together, finalize the still-open game rules, and use this material to guide the first implementation steps.

**Project resources:**

- [GitHub repository and SRS](https://github.com/fearless-dhbw/dom1no)
- [Detailed use cases](https://github.com/fearless-dhbw/dom1no/tree/main/docs/use-cases) *(link becomes active after the files are uploaded)*
- [UI mockups in Figma](https://www.figma.com/design/kJDrWOX1E7Uci3U2Gp9Cvs/Untitled?node-id=0-1&t=MYacNVOjFRvgLBVh-1)

*Images to insert in WordPress: gameplay activity diagram, friends activity diagram, one gameplay mockup, and one friends mockup. Add descriptive captions.*
