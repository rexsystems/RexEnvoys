package me.rexsystems.rexEnvoys;

import me.rexsystems.rexEnvoys.command.EnvoyCommand;
import me.rexsystems.rexEnvoys.config.PluginConfig;
import me.rexsystems.rexEnvoys.crate.CrateListener;
import me.rexsystems.rexEnvoys.envoy.EnvoyCatalog;
import me.rexsystems.rexEnvoys.envoy.EnvoyService;
import me.rexsystems.rexEnvoys.flare.FlareListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class RexEnvoys extends JavaPlugin {

    private PluginConfig pluginConfig;
    private EnvoyCatalog catalog;
    private EnvoyService envoys;

    @Override
    public void onEnable() {
        this.pluginConfig = new PluginConfig(this);
        this.pluginConfig.load();
        this.catalog = new EnvoyCatalog(this);
        this.catalog.load();
        this.envoys = new EnvoyService(this);
        this.envoys.startTicker();

        getServer().getPluginManager().registerEvents(new FlareListener(this), this);
        getServer().getPluginManager().registerEvents(new CrateListener(this), this);

        EnvoyCommand command = new EnvoyCommand(this);
        PluginCommand pluginCommand = getCommand("rexenvoys");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new me.rexsystems.rexEnvoys.papi.RexEnvoysPlaceholders(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        getLogger().info("RexEnvoys enabled.");
    }

    @Override
    public void onDisable() {
        if (envoys != null) {
            envoys.shutdown();
        }
        getLogger().info("RexEnvoys disabled.");
    }

    public void reloadPlugin() {
        pluginConfig.load();
        catalog.load();
    }

    public PluginConfig config() {
        return pluginConfig;
    }

    public EnvoyCatalog catalog() {
        return catalog;
    }

    public EnvoyService envoys() {
        return envoys;
    }
}
