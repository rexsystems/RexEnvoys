---
title: RexEnvoys
description: Supply crate events for Paper, Purpur, and Folia.
---

RexEnvoys drops supply crates. An event can start on a timer, from `/rexenvoys start`, or when a player right-clicks that event's flare. Two events can be active at the same time.

Each crate is a real chest (or barrel). The loot list is built once and stored on that crate. Opening it again, or taking items out, uses the same contents.

Messages use MiniMessage. Legacy `&` codes, `&#RRGGBB`, and `#RRGGBB` also work.

## Files

```
plugins/RexEnvoys/
  config.yml
  envoys.yml
```

`config.yml` holds shared settings and command text. A reload that changes the file writes `config.yml.bak.<timestamp>` first, then adds any keys that were missing. Values you already set stay as they are.

`envoys.yml` holds the events. It is written only when the file is missing. Reload reads it and leaves your edits in place.

## Next

- [Installation](/rexenvoys/getting-started/installation/)
- [Events](/rexenvoys/guides/events/)
- [Flares](/rexenvoys/guides/flares/)
- [Commands](/rexenvoys/reference/commands/)
