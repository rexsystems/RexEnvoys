---
title: Commands
description: RexEnvoys commands and their permissions.
sidebar:
  order: 1
---

| Command | Description | Permission |
|---|---|---|
| `/rexenvoys` | Show help | `rexenvoys.use` |
| `/rexenvoys help` | Show help | `rexenvoys.use` |
| `/rexenvoys list` | List every event and whether it is running | `rexenvoys.use` |
| `/rexenvoys start <id>` | Start one event | `rexenvoys.start` |
| `/rexenvoys stop <id>` | Stop that event and remove its crates | `rexenvoys.start` |
| `/rexenvoys flare give <player> <id>` | Give one flare | `rexenvoys.flare` |
| `/rexenvoys reload` | Reload `config.yml` and re-read `envoys.yml` | `rexenvoys.reload` |

Aliases: `/renvoy`, `/envoy`

Help text comes from `messages.help-header` and `messages.help`. Reload applies to the next start. Crates that are already on the ground keep the loot they already rolled.

Tab completion lists event ids for start, stop, and flare give, and online player names for the give target.
