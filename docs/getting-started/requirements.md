---
title: Requirements
description: What RexEnvoys needs to run.
sidebar:
  order: 2
---

- Java 21
- Paper, Purpur, or Folia 1.21 or newer
- `folia-supported` is set, and block changes run on the region that owns the crate

Optional plugins, listed as `softdepend`:

| Plugin | What it adds |
|---|---|
| PlaceholderAPI | `%rexenvoys_active%`, `%rexenvoys_ids%`, `%rexenvoys_running_<id>%` |
| ItemsAdder | Places a namespaced custom block when `block.id` points at ItemsAdder |
| Oraxen | Places a namespaced custom block when `block.id` points at Oraxen |

The plugin loads the ItemsAdder hook only after PluginManager reports ItemsAdder, and the Oraxen hook only after it reports Oraxen. A missing plugin does not load those classes.
