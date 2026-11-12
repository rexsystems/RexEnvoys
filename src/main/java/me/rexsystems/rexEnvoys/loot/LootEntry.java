package me.rexsystems.rexEnvoys.loot;

import java.util.List;

public record LootEntry(double chance, List<String> commands, List<LootItem> items) {

    public LootEntry {
        commands = commands == null ? List.of() : List.copyOf(commands);
        items = items == null ? List.of() : List.copyOf(items);
    }
}
