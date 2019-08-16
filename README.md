# ProtectBuild
A Spigot Minecraft Plugin

The goal is to _reduce_ griefing.

(Unfortunately there is a tradeoff between security and utility...)

## Features

* Registered players:
  * Requires a series of passphrases to log in.
  * Only registered players can do anything (period and literally).
* Tnt does not explode.
* Lava buckets are not _placeable_ and not _dispensable_.
* Limits entity _spawning_ within a chunk based on number of entities in chunk.
* Players instantly killed on attempting to cross worldborders.
* No enderdragons. (includes end crystal spawning)
* Automatically sets worldborders (for all dimensions) and spawn.
* Automatically sets gamerules to prevent:
  * Fire does not spread.
  * Low max entity cramming to help reduce entity lag. (defaults to 4)
  * No mobs griefing. (includes Withers)
* Spawn is on a stair at (0, 255, 0) and has a waterfall below.

## Coming soon

* Actually preventing players from destroying/interacting with blocks placed by other players
* Villager protection
* Tree farm at spawn (to prevent tree griefing)
