---
title: Flares
description: Give a flare and what a right-click does.
sidebar:
  order: 2
---

Each event has a `flare` section. The item is how a player calls that event without the start command.

```yaml
flare:
  material: BLAZE_ROD
  name: "<gold>Supply flare"
  lore:
    - "<gray>Right-click to call a supply drop."
  glow: true
  consume: true
```

`name` and `lore` use the same text parser as other messages. `glow` adds the enchantment glint. The event id is stored on the item.

Give one with:

```
/rexenvoys flare give <player> <id>
```

That needs `rexenvoys.flare`. The event has to exist and be enabled. If the inventory is full, the flare drops at the player's feet.

A right-click with that item calls the event. Only that interact is cancelled, so the flare does not also open a block or use the item as a normal tool. A right-click with any other item is left alone.

If the event is already running, the click is still cancelled and the flare stays in the inventory. The flare is consumed when the first crate of that call actually lands, and only when `consume` is true. Creative mode does not consume it. If no column can be placed, the player keeps the flare.

The event's `messages.flare` line is broadcast in the crate's world, with `{player}` set to whoever used it.
