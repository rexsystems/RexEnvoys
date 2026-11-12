package me.rexsystems.rexEnvoys.envoy;

import me.rexsystems.rexEnvoys.loot.LootEntry;
import org.bukkit.Material;

import java.util.List;

public record EnvoyDefinition(
        String id,
        boolean enabled,
        boolean scheduled,
        int intervalSeconds,
        boolean fixed,
        int amount,
        List<FixedPoint> points,
        Region region,
        Material material,
        String customBlockId,
        Flare flare,
        boolean firework,
        int timeoutSeconds,
        boolean despawnWhenEmpty,
        String lootWhen,
        SoundCue sound,
        TextLine actionBar,
        BossBarLine bossBar,
        Lines messages,
        List<LootEntry> loot
) {

    public EnvoyDefinition {
        points = points == null ? List.of() : List.copyOf(points);
        loot = loot == null ? List.of() : List.copyOf(loot);
        customBlockId = customBlockId == null ? "" : customBlockId;
        lootWhen = lootWhen == null ? "" : lootWhen;
    }

    public long intervalMillis() {
        return Math.max(1, intervalSeconds) * 1000L;
    }

    public record FixedPoint(String world, int x, int y, int z) {
    }

    public record Region(String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }

    public record Flare(Material material, String name, List<String> lore, boolean glow, boolean consume) {
        public Flare {
            lore = lore == null ? List.of() : List.copyOf(lore);
            name = name == null ? "" : name;
        }
    }

    public record SoundCue(boolean enabled, String sound, float volume, float pitch) {
    }

    public record TextLine(boolean enabled, String text) {
    }

    public record BossBarLine(boolean enabled, String text, String color, String style, int seconds) {
    }

    public record Lines(String start, String land, String open, String empty, String timeout, String flare) {
        public Lines {
            start = blank(start);
            land = blank(land);
            open = blank(open);
            empty = blank(empty);
            timeout = blank(timeout);
            flare = blank(flare);
        }

        private static String blank(String value) {
            return value == null ? "" : value;
        }
    }
}
