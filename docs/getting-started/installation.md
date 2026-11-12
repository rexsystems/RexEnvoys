---
title: Installation
description: Put RexEnvoys on a Paper, Purpur, or Folia server.
sidebar:
  order: 1
---

1. Download the jar.
2. Put it in the server `plugins/` folder.
3. Start the server once so `plugins/RexEnvoys/config.yml` and `envoys.yml` are written.
4. Edit both files. Change the sample region and the fixed point so they sit on your map.
5. Run `/rexenvoys reload`, or restart.

PlaceholderAPI is not required. If it is installed when RexEnvoys enables, the `%rexenvoys_...%` placeholders register on their own.

ItemsAdder and Oraxen are not required. A namespaced `block.id` is used only when that plugin is actually installed.
