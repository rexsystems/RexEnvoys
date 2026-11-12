package me.rexsystems.rexEnvoys.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

final class ItemsAdderHook {

    private ItemsAdderHook() {
    }

    static boolean place(String id, Location location) {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemsAdder");
            if (plugin == null) {
                return false;
            }
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> type = Class.forName("dev.lone.itemsadder.api.CustomBlock", true, loader);
            Method method = type.getMethod("place", String.class, Location.class);
            Object result = method.invoke(null, id, location);
            return !(result instanceof Boolean) || (Boolean) result;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
