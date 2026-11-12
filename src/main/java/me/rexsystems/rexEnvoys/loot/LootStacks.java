package me.rexsystems.rexEnvoys.loot;

import me.rexsystems.rexEnvoys.util.TextParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class LootStacks {

    private LootStacks() {
    }

    public static List<ItemStack> from(List<LootEntry> entries) {
        List<ItemStack> stacks = new ArrayList<>();
        if (entries == null) {
            return stacks;
        }
        for (LootEntry entry : entries) {
            for (LootItem item : entry.items()) {
                if (item.amount() < 1) {
                    continue;
                }
                Material material = Material.matchMaterial(item.material());
                if (material == null || !material.isItem() || material.isAir()) {
                    continue;
                }
                ItemStack base = new ItemStack(material);
                if (!item.name().isBlank() || !item.lore().isEmpty()) {
                    ItemMeta meta = base.getItemMeta();
                    if (!item.name().isBlank()) {
                        meta.displayName(TextParser.parse(item.name()));
                    }
                    if (!item.lore().isEmpty()) {
                        List<Component> lore = new ArrayList<>();
                        for (String line : item.lore()) {
                            lore.add(TextParser.parse(line));
                        }
                        meta.lore(lore);
                    }
                    base.setItemMeta(meta);
                }
                int left = item.amount();
                int max = Math.max(1, base.getMaxStackSize());
                while (left > 0) {
                    ItemStack part = base.clone();
                    int give = Math.min(max, left);
                    part.setAmount(give);
                    stacks.add(part);
                    left -= give;
                }
            }
        }
        return stacks;
    }
}
