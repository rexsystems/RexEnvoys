# RexEnvoys

Supply crate events for Paper, Purpur, and Folia. Run more than one event at a time. Each event keeps its own region, flare, loot, and messages.

**Java 21. Paper, Purpur, or Folia 1.21+.** PlaceholderAPI, ItemsAdder, and Oraxen are optional.

## Features

- Start on a timer, with `/rexenvoys start`, or by right-clicking a flare
- Random crates inside a region, or fixed points at the coordinates you wrote
- With safe spawn on, a random column is skipped when it is void, liquid, or not standing on a solid block
- Loot is rolled once into the chest. Taking items does not roll again
- Choose land or the first open for when that roll is written
- Firework, sound, action bar, and boss bar from the event's own config
- MiniMessage, `&` codes, `&#RRGGBB`, and `#RRGGBB` in player-facing text
- A namespaced block id uses ItemsAdder or Oraxen when that plugin is installed

## Commands

| Command | Description | Permission |
|---|---|---|
| `/rexenvoys help` | Show help | `rexenvoys.use` |
| `/rexenvoys list` | List events and whether each one is running | `rexenvoys.use` |
| `/rexenvoys start <id>` | Start an event | `rexenvoys.start` |
| `/rexenvoys stop <id>` | Stop an event and remove its crates | `rexenvoys.start` |
| `/rexenvoys flare give <player> <id>` | Give that event's flare | `rexenvoys.flare` |
| `/rexenvoys reload` | Reload `config.yml` and re-read `envoys.yml` | `rexenvoys.reload` |

Aliases: `/renvoy`, `/envoy`

## Permissions

| Permission | Default | Description |
|---|---|---|
| `rexenvoys.use` | true | Help and list |
| `rexenvoys.start` | op | Start and stop events |
| `rexenvoys.flare` | op | Give flares |
| `rexenvoys.reload` | op | Reload the plugin |

Documentation: https://docs.rexsystems.me/rexenvoys/
