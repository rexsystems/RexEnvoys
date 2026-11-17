# RexEnvoys

Supply crate events for Paper, Purpur, and Folia. Several events can run at once. Each event has its own schedule, region, flare, loot, and messages.

<p>
  <img src="https://img.shields.io/badge/Minecraft-1.21%2B-62B47A?style=for-the-badge" alt="Minecraft 1.21+">
  <img src="https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
</p>

## Features

- Schedule an event, or start it with a command or a flare
- Random columns inside a region, or fixed points used as written
- Safe random spawns skip void, liquid, and ground that is not solid
- One chest inventory per crate. Loot is rolled once, on land or on the first open
- Firework, sound, action bar, and boss bar per event
- MiniMessage, legacy `&` codes, `&#RRGGBB`, and `#RRGGBB`
- ItemsAdder and Oraxen block ids when those plugins are installed
- PlaceholderAPI for who is running

## Requirements

- Java 21
- Paper, Purpur, or Folia 1.21+
- PlaceholderAPI, ItemsAdder, and Oraxen are optional

## Install

1. Put the jar in `plugins/`
2. Restart
3. Edit `plugins/RexEnvoys/config.yml` and `plugins/RexEnvoys/envoys.yml`
4. Run `/rexenvoys reload`

`config.yml` picks up new keys on reload. `envoys.yml` is created when it is missing and is not overwritten after that.

## Commands

| Command | Permission |
|---|---|
| `/rexenvoys help` | `rexenvoys.use` |
| `/rexenvoys list` | `rexenvoys.use` |
| `/rexenvoys start <id>` | `rexenvoys.start` |
| `/rexenvoys stop <id>` | `rexenvoys.start` |
| `/rexenvoys flare give <player> <id>` | `rexenvoys.flare` |
| `/rexenvoys reload` | `rexenvoys.reload` |

Aliases: `/renvoy`, `/envoy`

## Documentation

The pages in [docs/](docs/index.md) drop into a Starlight site at `src/content/docs/rexenvoys/`. Paste `docs/sidebar.mjs` into the site sidebar.

Full docs: [https://docs.rexsystems.me/rexenvoys/](https://docs.rexsystems.me/rexenvoys/)

## License

Licensed under the [GNU Affero General Public License v3.0](LICENSE).
