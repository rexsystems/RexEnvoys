package me.rexsystems.rexEnvoys.config;

import me.rexsystems.rexEnvoys.RexEnvoys;

import java.util.ArrayList;
import java.util.List;

public final class PluginConfig {

    private final RexEnvoys plugin;

    public PluginConfig(RexEnvoys plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        new ConfigAutoUpdater(plugin).update();
        plugin.reloadConfig();
    }

    public boolean safeSpawn() {
        return plugin.getConfig().getBoolean("settings.safe-spawn", true);
    }

    public int spawnAttempts() {
        return Math.max(0, plugin.getConfig().getInt("settings.spawn-attempts", 12));
    }

    public String lootWhen() {
        String value = plugin.getConfig().getString("settings.loot-when", "open");
        return value == null || value.isBlank() ? "open" : value;
    }

    public String raw(String path) {
        String value = plugin.getConfig().getString("messages." + path);
        return value == null ? "" : value;
    }

    public String prefix() {
        return raw("prefix");
    }

    public List<String> helpLines() {
        List<String> lines = plugin.getConfig().getStringList("messages.help");
        String prefix = prefix();
        List<String> withPrefix = new ArrayList<>();
        for (String line : lines) {
            withPrefix.add(line == null ? "" : line.replace("{prefix}", prefix).replace("<prefix>", prefix));
        }
        return withPrefix;
    }
}
