---
title: Settings
description: Shared settings in config.yml.
sidebar:
  order: 1
---

`config.yml` is the shared file. Event definitions live in `envoys.yml`. See [Events](/rexenvoys/guides/events/).

```yaml
settings:
  loot-when: open
  safe-spawn: true
  spawn-attempts: 12
```

| Key | Meaning |
|---|---|
| `settings.loot-when` | `open` writes the roll on the first time a player opens the chest. `land` writes it when the crate is placed. An event can set its own `loot-when` and leave this blank to inherit. |
| `settings.safe-spawn` | When true, a random column is rejected if it is below the world minimum, liquid, or not standing on a solid block. Fixed points ignore this. |
| `settings.spawn-attempts` | How many random columns to try for one crate. After that, that crate is skipped. |

`loot-when` does not roll again later. The chest keeps the list from the first write.

Command text is under `messages`. See [Messages](/rexenvoys/configuration/messages/).
