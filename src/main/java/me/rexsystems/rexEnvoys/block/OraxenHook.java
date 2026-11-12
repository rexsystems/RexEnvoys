package me.rexsystems.rexEnvoys.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

final class OraxenHook {

    private OraxenHook() {
    }

    static boolean place(String id, Location location) {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Oraxen");
            if (plugin == null) {
                return false;
            }
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> type = Class.forName("io.th0rgal.oraxen.api.OraxenBlocks", true, loader);
            Method method = type.getMethod("place", String.class, Location.class);
            method.invoke(null, id, location);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
