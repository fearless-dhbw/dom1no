# Domino rules

These rules describe the intended online game, agreed with the team on 9 November 2026. They replace the previous draft's undefined “Rules Profile.” They are a specification, not evidence of a working game.

## Choose a match

| Configuration | Players | Competition | Tiles not dealt |
| --- | --- | --- | --- |
| Individual draw | 2 | Each player plays for themselves | 14 tiles form the boneyard |
| Individual draw | 3 | Each player plays for themselves | 7 tiles form the boneyard |
| Partnership block | 4 | Opposite seats are partners: 1 and 3 against 2 and 4 | None |

Players choose one configuration before entering public matchmaking. They wait for that configuration: the system does not silently change player count or variant when opponents are unavailable. In a private room, the host chooses the configuration, invitees can see it, and the host can start only when all required seats are occupied.

Waiting-time limits and changing or cancelling a queue request are not specified yet. They do not authorize automatic fallback.

## Set up the board

Use one double-six set: the 28 distinct unordered pairs from `0|0` through `6|6`. Shuffle the set and deal seven tiles to each player. The remaining tiles, if any, stay face down in the boneyard.

In four-player partnership play, the holder of `6|6` opens with that tile. In individual play, the highest dealt double opens. If no player has a double, the tile with the highest sum of pips opens; the higher end breaks a tie. For example, `6|3` starts ahead of `5|4` when both total nine. A double in the boneyard cannot start the game.

Turns follow seat order, wrapping from the last seat to seat 1. The opener's placement counts as their turn.

## Take a turn

Place one tile from your hand at either open end of the chain. Rotate it if necessary so that the touching values match. Doubles have no special branching behavior: the board remains one chain with two open ends.

For example, with open ends **3 and 1**, `3|5` can be placed on the 3 end. The new ends are **5 and 1**, and the tile leaves the player's hand. `6|6` cannot be placed at either end. Rejecting it leaves the board, hand and turn unchanged.

Only the active player can act. A rejected placement does not consume a turn. A legal placement resets the consecutive-pass count to zero.

### When no tile fits

- **Individual draw:** draw tiles until a legal tile is obtained or the boneyard is empty. Play the obtained legal tile. If no tile can be played and the boneyard is empty, pass. A player who already has a legal placement cannot draw or pass.
- **Partnership block:** there is no boneyard. Pass only when no tile in the hand can be placed.

A pass advances the turn and increases the consecutive-pass count by one. A later placement resets that count; passes separated by a placement do not accumulate toward a blocked match.

## End the match

An empty hand ends the match immediately. In individual play, that player wins. In partnership play, that player's team wins. No further turn is taken.

A match is blocked when every participant has passed consecutively: two, three or four passes according to the selected configuration. Individual players can pass only after the boneyard is empty.

For a blocked match, count the pips remaining in each hand, not the number of tiles:

- **Individual:** the lowest personal pip total wins. Players tied for the lowest total share a draw.
- **Partnership:** add both partners' pip totals. The team with the lower sum wins; equal team sums produce a draw.

For example, a hand containing only `6|6` has **12 pips**. A hand containing `0|0` and `1|1` has **2 pips** and wins that blocked comparison despite containing more tiles. With those as the only remaining hands and open ends 2 and 2, neither player can move.

One match is one game. There is no target score, accumulated multi-round score or ranking by remaining tile count.
