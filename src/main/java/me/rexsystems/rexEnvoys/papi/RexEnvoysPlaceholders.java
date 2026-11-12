package me.rexsystems.rexEnvoys.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.rexsystems.rexEnvoys.RexEnvoys;
import me.rexsystems.rexEnvoys.envoy.ActiveEnvoys;
import me.rexsystems.rexEnvoys.envoy.EnvoyDefinition;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class RexEnvoysPlaceholders extends PlaceholderExpansion {

    private final RexEnvoys plugin;

    public RexEnvoysPlaceholders(RexEnvoys plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rexenvoys";
    }

    @Override
    public @NotNull String getAuthor() {
        return "RexSystems";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        ActiveEnvoys active = plugin.envoys().active();
        String key = params.toLowerCase(Locale.ROOT);
        if (key.equals("active")) {
            return Integer.toString(active.view().size());
        }
        if (key.equals("ids")) {
            return String.join(", ", active.view());
        }
        if (key.startsWith("running_")) {
            String id = params.substring("running_".length());
            EnvoyDefinition definition = plugin.catalog().find(id);
            String canonical = definition == null ? id : definition.id();
            return active.contains(canonical) ? "yes" : "no";
        }
        return null;
    }
}
