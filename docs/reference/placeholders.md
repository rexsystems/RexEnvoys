---
title: Placeholders
description: PlaceholderAPI tokens from RexEnvoys.
sidebar:
  order: 3
---

These register when PlaceholderAPI is installed at enable. Reload does not register them if PlaceholderAPI was added later. Restart after adding it.

| Placeholder | Description |
|---|---|
| `%rexenvoys_active%` | How many events are running |
| `%rexenvoys_ids%` | Running event ids, separated by commas |
| `%rexenvoys_running_<id>%` | `yes` or `no` for that event id |

Inside messages, the tokens in [Messages](/rexenvoys/configuration/messages/) are separate from PlaceholderAPI. Both can appear in the same line.
