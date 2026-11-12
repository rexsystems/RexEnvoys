package me.rexsystems.rexEnvoys.envoy;

import me.rexsystems.rexEnvoys.RexEnvoys;
import me.rexsystems.rexEnvoys.loot.LootEntry;
import me.rexsystems.rexEnvoys.loot.LootItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EnvoyCatalog {

    private final RexEnvoys plugin;
    private List<EnvoyDefinition> events = List.of();

    public EnvoyCatalog(RexEnvoys plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create the plugin folder.");
        }
        File file = new File(plugin.getDataFolder(), "envoys.yml");
        if (!file.exists()) {
            plugin.saveResource("envoys.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        this.events = parse(yaml);
    }

    public List<EnvoyDefinition> all() {
        return events;
    }

    public EnvoyDefinition find(String id) {
        if (id == null) {
            return null;
        }
        for (EnvoyDefinition event : events) {
            if (event.id().equalsIgnoreCase(id)) {
                return event;
            }
        }
        return null;
    }

    private List<EnvoyDefinition> parse(YamlConfiguration yaml) {
        ConfigurationSection root = yaml.getConfigurationSection("events");
        if (root == null) {
            return List.of();
        }
        List<EnvoyDefinition> parsed = new ArrayList<>();
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null || id.isBlank()) {
                continue;
            }
            try {
                parsed.add(read(id, section));
            } catch (RuntimeException failure) {
                plugin.getLogger().warning("Skipped event '" + id + "': " + failure.getMessage());
            }
        }
        return List.copyOf(parsed);
    }

    private static EnvoyDefinition read(String id, ConfigurationSection section) {
        ConfigurationSection region = section.getConfigurationSection("region");
        ConfigurationSection min = region == null ? null : region.getConfigurationSection("min");
        ConfigurationSection max = region == null ? null : region.getConfigurationSection("max");
        String world = region == null ? "world" : region.getString("world", "world");
        EnvoyDefinition.Region bounds = new EnvoyDefinition.Region(
                world == null || world.isBlank() ? "world" : world,
                coord(min, "x", -128),
                coord(min, "y", 60),
                coord(min, "z", -128),
                coord(max, "x", 128),
                coord(max, "y", 100),
                coord(max, "z", 128)
        );

        List<EnvoyDefinition.FixedPoint> points = new ArrayList<>();
        for (Map<?, ?> row : section.getMapList("spawn.points")) {
            points.add(new EnvoyDefinition.FixedPoint(
                    text(row.get("world"), bounds.world()),
                    (int) number(row.get("x"), 0),
                    (int) number(row.get("y"), 64),
                    (int) number(row.get("z"), 0)
            ));
        }

        String mode = section.getString("spawn.mode", "random");
        boolean fixed = mode != null && mode.equalsIgnoreCase("fixed");
        String trigger = section.getString("trigger", "manual");
        boolean scheduled = trigger != null && trigger.equalsIgnoreCase("schedule");
        ConfigurationSection flare = section.getConfigurationSection("flare");
        Material flareMaterial = itemMaterial(flare == null ? null : flare.getString("material"), Material.BLAZE_ROD);
        ConfigurationSection sound = section.getConfigurationSection("sound");
        ConfigurationSection actionbar = section.getConfigurationSection("actionbar");
        ConfigurationSection bossbar = section.getConfigurationSection("bossbar");
        ConfigurationSection messages = section.getConfigurationSection("messages");
        int timeout = section.contains("timeout-seconds") ? section.getInt("timeout-seconds") : 300;

        return new EnvoyDefinition(
                id,
                section.getBoolean("enabled", true),
                scheduled,
                Math.max(1, section.getInt("interval-seconds", 1800)),
                fixed,
                Math.max(0, section.getInt("spawn.amount", 1)),
                points,
                bounds,
                blockMaterial(section.getString("block.material")),
                section.getString("block.id", ""),
                new EnvoyDefinition.Flare(
                        flareMaterial,
                        flare == null ? "<gold>" + id + " flare" : flare.getString("name", "<gold>" + id + " flare"),
                        flare == null ? List.of() : flare.getStringList("lore"),
                        flare == null || flare.getBoolean("glow", true),
                        flare == null || flare.getBoolean("consume", true)
                ),
                section.getBoolean("firework", true),
                timeout,
                section.getBoolean("despawn-when-empty", true),
                section.getString("loot-when", ""),
                new EnvoyDefinition.SoundCue(
                        sound != null && sound.getBoolean("enabled", false),
                        sound == null ? "" : sound.getString("sound", ""),
                        (float) (sound == null ? 1.0 : sound.getDouble("volume", 1.0)),
                        (float) (sound == null ? 1.0 : sound.getDouble("pitch", 1.0))
                ),
                new EnvoyDefinition.TextLine(
                        actionbar != null && actionbar.getBoolean("enabled", false),
                        actionbar == null ? "" : actionbar.getString("text", "")
                ),
                new EnvoyDefinition.BossBarLine(
                        bossbar != null && bossbar.getBoolean("enabled", false),
                        bossbar == null ? "" : bossbar.getString("text", ""),
                        bossbar == null ? "YELLOW" : bossbar.getString("color", "YELLOW"),
                        bossbar == null ? "SOLID" : bossbar.getString("style", "SOLID"),
                        bossbar == null ? 8 : Math.max(1, Math.min(60, bossbar.getInt("seconds", 8)))
                ),
                new EnvoyDefinition.Lines(
                        messages == null ? "" : messages.getString("start", ""),
                        messages == null ? "" : messages.getString("land", ""),
                        messages == null ? "" : messages.getString("open", ""),
                        messages == null ? "" : messages.getString("empty", ""),
                        messages == null ? "" : messages.getString("timeout", ""),
                        messages == null ? "" : messages.getString("flare", "")
                ),
                loot(section)
        );
    }

    private static List<LootEntry> loot(ConfigurationSection section) {
        List<LootEntry> entries = new ArrayList<>();
        for (Map<?, ?> row : section.getMapList("loot")) {
            List<LootItem> items = new ArrayList<>();
            Object rawItems = row.get("items");
            if (rawItems instanceof List<?> list) {
                for (Object item : list) {
                    LootItem parsed = item(item);
                    if (parsed != null) {
                        items.add(parsed);
                    }
                }
            }
            entries.add(new LootEntry(number(row.get("chance"), 100), strings(row.get("commands")), items));
        }
        return entries;
    }

    private static LootItem item(Object raw) {
        if (raw instanceof ConfigurationSection section) {
            return new LootItem(
                    section.getString("material", "STONE"),
                    section.getInt("amount", 1),
                    section.getString("name", ""),
                    section.getStringList("lore")
            );
        }
        if (raw instanceof Map<?, ?> map) {
            return new LootItem(
                    text(map.get("material"), "STONE"),
                    (int) number(map.get("amount"), 1),
                    text(map.get("name"), ""),
                    strings(map.get("lore"))
            );
        }
        return null;
    }

    private static List<String> strings(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (Object value : list) {
            if (value != null) {
                values.add(String.valueOf(value));
            }
        }
        return values;
    }

    private static int coord(ConfigurationSection section, String key, int fallback) {
        if (section == null) {
            return fallback;
        }
        return section.getInt(key, fallback);
    }

    private static String text(Object raw, String fallback) {
        if (raw == null) {
            return fallback;
        }
        String value = String.valueOf(raw);
        return value.isBlank() ? fallback : value;
    }

    private static double number(Object raw, double fallback) {
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        if (raw == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(String.valueOf(raw).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static Material blockMaterial(String name) {
        Material material = name == null ? null : Material.matchMaterial(name.trim().toUpperCase(Locale.ROOT));
        if (material == null || !material.isBlock()) {
            return Material.CHEST;
        }
        return material;
    }

    private static Material itemMaterial(String name, Material fallback) {
        Material material = name == null ? null : Material.matchMaterial(name.trim().toUpperCase(Locale.ROOT));
        if (material == null || !material.isItem() || material.isAir()) {
            return fallback;
        }
        return material;
    }
}
