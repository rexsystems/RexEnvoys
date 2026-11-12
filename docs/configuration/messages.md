---
title: Messages
description: Command text and the tokens events can use.
sidebar:
  order: 2
---

Command replies live in `config.yml` under `messages`. Event broadcasts live on each event in `envoys.yml` under `messages`, `actionbar`, and `bossbar`.

All of those strings go through the same parser. MiniMessage, `&` codes, `&#RRGGBB`, and `#RRGGBB` work. A blank string is not sent.

`{prefix}` is the `messages.prefix` value, inserted with its colors intact. Other tokens are inserted as plain text:

| Token | Meaning |
|---|---|
| `{prefix}` | The prefix from `config.yml` |
| `{id}` | Event id |
| `{player}` | The player who opened the crate, used the flare, or received one |
| `{world}` | World name |
| `{x}` `{y}` `{z}` | Crate block coordinates |
| `{state}` | `running` or `idle`, on the list line |

PlaceholderAPI placeholders in a player-facing string are resolved for that player when PlaceholderAPI is installed.

The action bar and boss bar are sent when the first crate of a wave lands. The land line is sent for every crate. Boss bar `color` is `PINK`, `BLUE`, `RED`, `GREEN`, `YELLOW`, `PURPLE`, or `WHITE`. `style` accepts `SOLID` or `SEGMENTED_6`, `SEGMENTED_10`, `SEGMENTED_12`, and `SEGMENTED_20`. `seconds` is clamped between 1 and 60.
