# Last Man Standing

This is the original *Speed Survival* game mode, as previously seen on http://hcmcpvp.com/

The game mode has been decommissioned, and is now free to use by anyone (and open source).

Gameplay:
- Players start on a randomly generated world. The plugin ensures that the world is not an ocean world.
- Upon game start, players have to gather supplies, as in every other survival game mode.
- Players are invincible for the first minute of the game, after that time they can attack and be attacked by others at will.
- At the morning of the second day, a feast spawns at X & Z = 0, containing diamond quality gear, potions and other valuables.

Features:
- Worlds are generated on-demand, depending on the amount of players.
- The world size is controlled by Bukkit. A small world (500 or less) is recommended.
- Players have a tracking compass that shows the position of the nearest player **outside of a 20 block range** (useful for forming teams)

Permissions:
- *lms.spectatorchat* allows spectators to chat with one another. Players without this permission will receive a no-permission message.

**Note:** Server OPs cannot play, as it is expected they monitor the game in spectator mode and ensure no one is cheating. If an OP wants to play, they need to deop themselves and re-join the server.

This plugin is fully compatible with ProtocolSupport, TerrainControl and Orebfuscator.

This plugin is under semi-active development. New features are being added, and existing features are being improved, but it is low priority right now. Feel free to contribute on GitHub!

## GitHub Info

The `release` branch contains the latest stable release. New branches with may pop up from time to time, the latest version branch will contain the most recent features and is mostly in line with `master`, and thus is often buggy. Bugfixes will always be applied to `master` first, and will be shipped in the next version.

When contributing, make sure to submit your pull request to `master`.
