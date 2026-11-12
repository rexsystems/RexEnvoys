package me.rexsystems.rexEnvoys.util;

import org.bukkit.Sound;

import java.util.Locale;

public final class Sounds {

    private Sounds() {
    }

    public static Sound resolve(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String key = name.trim().toUpperCase(Locale.ROOT).replace('.', '_').replace(' ', '_').replace('-', '_');
        try {
            return Sound.valueOf(key);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
