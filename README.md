# ProtectBuild
A Spigot Minecraft 1.14 Plugin

**This is currently in _BETA_. Use at your own risk.**

The goal is to _reduce_ griefing.

(Unfortunately there is a tradeoff between security and utility...)

## Features

* Registered players:
  * Requires a series of passphrases to log in.
  * Only registered players can do anything (period and literally).
* Players cannot destroy or interact with blocks placed by other players.
(With the exception of interacting with crafting and echanting tables.)
* Villager and wandering trader protection.
* Tree farm at spawn (to prevent tree griefing).
Saplings can only grow near spawn.
* Preventing all portal creation not from normal world.
* Set home and teleport home command.
* Teleport spawn command.
* Tnt does not explode.
* Lava buckets are not _placeable_ and not _dispensable_.
* Limits entity _spawning_ within a chunk based on number of entities that in chunk.
* Players instantly killed on attempting to cross worldborders.
* No enderdragons. (includes end crystal spawning)
* Automatically sets worldborders (for all dimensions).
* Spawn set to (0, 255, 0) for normal world.
* Spawn is on a stair at (0, 255, 0) and has a waterfall below.
* Automatically sets gamerules to prevent:
  * Fire does not spread.
  * Low max entity cramming to help reduce entity lag. (defaults to 4)
  * No mobs griefing. (includes Withers)

## Assumptions
The server is using the normal "3" world system. (You can disable nether or the end dimensions.)

## Motivation

The core idea is that players should be able to build and have that build be off limits to other players.

## Issues

Minecraft is complex and this plugin likely has not covered all the corners. Pistons for example are still allowed. However, an emergent property from this plugin is that one can build a secure base using furnaces!

It is hard to have the freedom of playing multiplayer vanilla Minecraft without a tradeoff with "grief security".

You can also build around a build and that is a form of griefing. This leads me to todos.

## Todo

* Admin can get info about who owns a block.
* Admin can remove blocks.
