---
title: Events
description: How an event in envoys.yml spawns crates.
sidebar:
  order: 1
---

Each key under `events` is an id. `supply` and `airdrop` ship as examples. You can run both at once: starting one does not stop the other.

```yaml
events:
  supply:
    enabled: true
    trigger: schedule
    interval-seconds: 1800
    spawn:
      mode: random
      amount: 3
    region:
      world: world
      min: { x: -128, y: 60, z: -128 }
      max: { x: 128, y: 100, z: 128 }
```

## Trigger

`schedule` waits one interval after the server starts, then starts the event if it is not already running. If it is still running when the interval is up, the next attempt waits another full interval.

`manual` starts only from `/rexenvoys start <id>` or from that event's flare.

## Spawn

`spawn.mode: random` picks `spawn.amount` columns inside `region`. With `settings.safe-spawn` true, each try is dropped when the Y is below the world minimum, the column is liquid, or the block under the crate is not solid. The destination itself has to be empty. After `settings.spawn-attempts` misses, that one crate is skipped.

`spawn.mode: fixed` places one crate at each entry under `spawn.points`. Those coordinates are used as written, including mid-air. `region` is ignored for the placement. `amount` is ignored too.

## Block

```yaml
block:
  material: CHEST
  id: ""
```

`material` should be `CHEST`, `TRAPPED_CHEST`, or `BARREL`. That block's inventory is the crate. Players open it the normal way.

`id` is optional. A value with a colon is a namespaced custom block:

| id | Plugin |
|---|---|
| `namespace:block` | ItemsAdder if it is installed, otherwise Oraxen |
| `itemsadder:namespace:block` | ItemsAdder |
| `oraxen:block_id` | Oraxen |

If the named plugin is not installed, the crate uses `material` and RexEnvoys logs that once. If the custom block is not a container, the crate is placed as `material` so the loot still has a chest inventory, and that is logged once too.

## Loot

```yaml
loot:
  - chance: 100
    commands:
      - "give {player} emerald 1"
    items:
      - material: DIAMOND
        amount: 2
        name: "<aqua>Supply diamond"
        lore:
          - "<gray>From a supply crate"
```

`chance` is 0 to 100. Entries at 100 are always included. Anything between is drawn once per crate. The winning entries are kept. The chest is filled from that list a single time.

`settings.loot-when: land` fills the chest when the crate is placed. `open` fills it on the first right-click, before the chest opens. Later opens see the same inventory, minus whatever was taken.

Commands run once, as console, at that same moment. `{player}` is the opener, or the player who used the flare when the loot is written on land. Stacks that do not fit in the chest drop on the crate.

## Lifetime

`timeout-seconds` removes the crate after that many seconds. `0` leaves it until it is emptied or stopped. On timeout, items still inside are deleted with the block.

`despawn-when-empty` removes the crate when the chest has no items left, after the loot has been written. An unopened chest in `open` mode stays put, because it has not been filled yet.

Empty and timeout removal runs on the region scheduler for that block. The chest and the marker entity at that spot are both removed there. `/rexenvoys stop <id>` removes every crate for that event the same way.

`firework: true` bursts a firework when the crate lands. `sound` plays at the block when `sound.enabled` is true. Use a Bukkit sound name such as `ENTITY_FIREWORK_ROCKET_LARGE_BLAST`.
