package me.rexsystems.rexEnvoys;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {

    private Keys() {
    }

    public static NamespacedKey flare(Plugin plugin) {
        return new NamespacedKey(plugin, "flare");
    }

    public static NamespacedKey crate(Plugin plugin) {
        return new NamespacedKey(plugin, "crate");
    }
}
