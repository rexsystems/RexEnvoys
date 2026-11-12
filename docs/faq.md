---
title: FAQ
description: Loot rolls, missing block plugins, and where crates are removed.
sidebar:
  order: 1
---

## Does opening the chest roll the loot again?

Each crate builds its loot list once. `settings.loot-when: land` does that when the crate is placed. `open` does it on the first right-click, and the chest then opens with those items.

The list is cached on the crate. A second request returns that same list and does not draw chances again. The items are written into that one chest inventory. Taking a stack, shift-clicking, or letting a hopper pull an item only changes that inventory. RexEnvoys does not refill it.

If the winning items do not fit, the extra stacks drop on the crate at the moment of that single fill.

## ItemsAdder is not installed

Set `block.id` only when you want a custom block. A value that contains `:` is namespaced.

If ItemsAdder is missing, RexEnvoys places `block.material` instead and writes one warning. It does not try to load the ItemsAdder hook. The same rule applies to Oraxen: the Oraxen hook loads only when PluginManager reports that Oraxen is there. If both are missing, there is still a single warning, and every namespaced id uses the configured material.

If the custom block places but is not a chest, barrel, or trapped chest, RexEnvoys swaps in `block.material` so the crate has a real inventory, and logs that once.

## Where does the crate get removed?

An empty chest, after the loot has been written, and a crate that hits `timeout-seconds`, are removed on the region scheduler. The call is `SchedulerUtils.runAtLocation` for that block. On Folia that is the thread allowed to change the block. The chest is set to air and the marker entity on that spot is removed in the same task.

`/rexenvoys stop <id>` uses that same region task for each crate the event still has.

## The scheduled event did not start on reboot

The first run waits `interval-seconds`. A reload does not reset a timer that is already counting, and it does not overwrite `envoys.yml`.

## A flare right-click also used the block

Only the interact whose item is a RexEnvoys flare is cancelled. A right-click with a different item is passed through, including while a crate is open in another inventory. Crate clicks are ordinary chest clicks. RexEnvoys does not cancel them.

## Reload did not add my new event

Reload re-reads `envoys.yml` from disk. It does not copy the default file over yours. Add the event to the file you already have, then run `/rexenvoys reload`.
