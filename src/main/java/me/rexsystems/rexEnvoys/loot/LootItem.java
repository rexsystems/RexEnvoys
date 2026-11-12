package me.rexsystems.rexEnvoys.loot;

import java.util.List;

public record LootItem(String material, int amount, String name, List<String> lore) {

    public LootItem {
        material = material == null || material.isBlank() ? "STONE" : material;
        name = name == null ? "" : name;
        lore = lore == null ? List.of() : List.copyOf(lore);
    }
}
