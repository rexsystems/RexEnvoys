package me.rexsystems.rexEnvoys.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CustomBlockPlacer {

    private static final Set<String> LOGGED = ConcurrentHashMap.newKeySet();

    private CustomBlockPlacer() {
    }

    public static void place(Plugin plugin, Block block, String customId, Material material) {
        Material fallback = material == null || !material.isBlock() ? Material.CHEST : material;
        if (customId == null || customId.isBlank() || customId.indexOf(':') < 0) {
            block.setType(fallback, false);
            return;
        }

        String id = customId.trim();
        boolean itemsAdder = !id.regionMatches(true, 0, "oraxen:", 0, "oraxen:".length());
        boolean oraxen = !id.regionMatches(true, 0, "itemsadder:", 0, "itemsadder:".length());
        if (id.regionMatches(true, 0, "itemsadder:", 0, "itemsadder:".length())) {
            id = id.substring("itemsadder:".length());
        } else if (id.regionMatches(true, 0, "oraxen:", 0, "oraxen:".length())) {
            id = id.substring("oraxen:".length());
        }

        PluginManager plugins = Bukkit.getPluginManager();
        boolean itemsAdderPresent = plugins.getPlugin("ItemsAdder") != null;
        boolean oraxenPresent = plugins.getPlugin("Oraxen") != null;
        Location location = block.getLocation();

        // Hook classes load only after PluginManager reports that plugin.
        if (itemsAdder && itemsAdderPresent && ItemsAdderHook.place(id, location)) {
            return;
        }
        if (oraxen && oraxenPresent && OraxenHook.place(id, location)) {
            return;
        }

        if (itemsAdder && !itemsAdderPresent && oraxen && !oraxenPresent) {
            warn(plugin, "missing-both", "ItemsAdder and Oraxen are not installed. Namespaced block ids fall back to the configured material.");
        } else if (itemsAdder && !oraxen && !itemsAdderPresent) {
            warn(plugin, "missing-itemsadder", "ItemsAdder is not installed. Namespaced block ids fall back to the configured material.");
        } else if (oraxen && !itemsAdder && !oraxenPresent) {
            warn(plugin, "missing-oraxen", "Oraxen is not installed. Namespaced block ids fall back to the configured material.");
        } else if (!itemsAdderPresent && !oraxenPresent) {
            warn(plugin, "missing-both", "ItemsAdder and Oraxen are not installed. Namespaced block ids fall back to the configured material.");
        } else {
            warn(plugin, "place-failed:" + customId, "Custom block '" + customId + "' could not be placed. Using " + fallback.name() + ".");
        }
        block.setType(fallback, false);
    }

    private static void warn(Plugin plugin, String key, String message) {
        if (LOGGED.add(key)) {
            plugin.getLogger().warning(message);
        }
    }
}
