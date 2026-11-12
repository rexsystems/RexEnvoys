package me.rexsystems.rexEnvoys.flare;

import me.rexsystems.rexEnvoys.Keys;
import me.rexsystems.rexEnvoys.RexEnvoys;
import me.rexsystems.rexEnvoys.envoy.EnvoyDefinition;
import me.rexsystems.rexEnvoys.util.TextParser;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class FlareItems {

    private FlareItems() {
    }

    public static ItemStack create(RexEnvoys plugin, EnvoyDefinition definition) {
        EnvoyDefinition.Flare flare = definition.flare();
        ItemStack stack = new ItemStack(flare.material());
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        if (flare.name() != null && !flare.name().isBlank()) {
            meta.displayName(TextParser.parse(flare.name()));
        }
        if (!flare.lore().isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : flare.lore()) {
                lore.add(TextParser.parse(line));
            }
            meta.lore(lore);
        }
        if (flare.glow()) {
            meta.setEnchantmentGlintOverride(true);
        }
        meta.getPersistentDataContainer().set(Keys.flare(plugin), PersistentDataType.STRING, definition.id());
        stack.setItemMeta(meta);
        return stack;
    }

    public static String eventId(RexEnvoys plugin, ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) {
            return null;
        }
        String id = stack.getItemMeta().getPersistentDataContainer().get(Keys.flare(plugin), PersistentDataType.STRING);
        if (id == null || id.isBlank()) {
            return null;
        }
        return id;
    }
}
