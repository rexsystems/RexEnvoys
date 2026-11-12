package me.rexsystems.rexEnvoys.command;

import me.rexsystems.rexEnvoys.RexEnvoys;
import me.rexsystems.rexEnvoys.envoy.EnvoyDefinition;
import me.rexsystems.rexEnvoys.flare.FlareItems;
import me.rexsystems.rexEnvoys.util.MessageService;
import me.rexsystems.rexEnvoys.util.Placeholders;
import me.rexsystems.rexEnvoys.util.SchedulerUtils;
import me.rexsystems.rexEnvoys.util.Tokens;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EnvoyCommand implements CommandExecutor, TabCompleter {

    private final RexEnvoys plugin;

    public EnvoyCommand(RexEnvoys plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return help(sender);
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list" -> list(sender);
            case "reload" -> reload(sender);
            case "start" -> start(sender, args);
            case "stop" -> stop(sender, args);
            case "flare" -> flare(sender, args);
            default -> help(sender);
        };
    }

    private boolean help(CommandSender sender) {
        if (!sender.hasPermission("rexenvoys.use")) {
            send(sender, "no-permission", Map.of());
            return true;
        }
        send(sender, "help-header", Map.of());
        for (String line : plugin.config().helpLines()) {
            MessageService.send(sender, line);
        }
        return true;
    }

    private boolean list(CommandSender sender) {
        if (!sender.hasPermission("rexenvoys.use")) {
            send(sender, "no-permission", Map.of());
            return true;
        }
        List<EnvoyDefinition> events = plugin.catalog().all();
        if (events.isEmpty()) {
            send(sender, "list-empty", Map.of());
            return true;
        }
        send(sender, "list-header", Map.of());
        for (EnvoyDefinition definition : events) {
            String state = plugin.envoys().active().contains(definition.id()) ? "running" : "idle";
            send(sender, "list-line", Map.of("id", definition.id(), "state", state));
        }
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission("rexenvoys.reload")) {
            send(sender, "no-permission", Map.of());
            return true;
        }
        plugin.reloadPlugin();
        send(sender, "reload", Map.of());
        return true;
    }

    private boolean start(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rexenvoys.start")) {
            send(sender, "no-permission", Map.of());
            return true;
        }
        if (args.length < 2) {
            send(sender, "usage-start", Map.of());
            return true;
        }
        plugin.envoys().start(sender, args[1]);
        return true;
    }

    private boolean stop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rexenvoys.start")) {
            send(sender, "no-permission", Map.of());
            return true;
        }
        if (args.length < 2) {
            send(sender, "usage-stop", Map.of());
            return true;
        }
        plugin.envoys().stop(sender, args[1]);
        return true;
    }

    private boolean flare(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rexenvoys.flare")) {
            send(sender, "no-permission", Map.of());
            return true;
        }
        if (args.length < 4 || !args[1].equalsIgnoreCase("give")) {
            send(sender, "usage-flare", Map.of());
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            send(sender, "player-offline", Map.of());
            return true;
        }
        EnvoyDefinition definition = plugin.catalog().find(args[3]);
        if (definition == null) {
            send(sender, "unknown-event", Map.of("id", args[3]));
            return true;
        }
        if (!definition.enabled()) {
            send(sender, "disabled", Map.of("id", definition.id()));
            return true;
        }
        ItemStack flare = FlareItems.create(plugin, definition);
        SchedulerUtils.runForPlayer(plugin, target, () -> {
            Map<Integer, ItemStack> leftover = target.getInventory().addItem(flare);
            for (ItemStack drop : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), drop);
            }
            send(target, "flare-received", Map.of("id", definition.id(), "player", target.getName()));
        });
        send(sender, "flare-given", Map.of("id", definition.id(), "player", target.getName()));
        return true;
    }

    private void send(CommandSender sender, String path, Map<String, String> extra) {
        Map<String, String> tokens = new HashMap<>();
        if (extra != null) {
            tokens.putAll(extra);
        }
        tokens.put("prefix", plugin.config().prefix());
        String text = Tokens.apply(plugin.config().raw(path), tokens);
        if (sender instanceof Player player) {
            text = Placeholders.apply(player, text);
        }
        MessageService.send(sender, text);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission("rexenvoys.use")) {
                options.add("help");
                options.add("list");
            }
            if (sender.hasPermission("rexenvoys.start")) {
                options.add("start");
                options.add("stop");
            }
            if (sender.hasPermission("rexenvoys.flare")) {
                options.add("flare");
            }
            if (sender.hasPermission("rexenvoys.reload")) {
                options.add("reload");
            }
            return filter(options, args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop"))
                && sender.hasPermission("rexenvoys.start")) {
            return filter(ids(), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("flare") && sender.hasPermission("rexenvoys.flare")) {
            return filter(List.of("give"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("flare") && args[1].equalsIgnoreCase("give")
                && sender.hasPermission("rexenvoys.flare")) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return filter(names, args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("flare") && args[1].equalsIgnoreCase("give")
                && sender.hasPermission("rexenvoys.flare")) {
            return filter(ids(), args[3]);
        }
        return List.of();
    }

    private List<String> ids() {
        List<String> ids = new ArrayList<>();
        for (EnvoyDefinition definition : plugin.catalog().all()) {
            ids.add(definition.id());
        }
        return ids;
    }

    private static List<String> filter(List<String> options, String prefix) {
        String needle = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(needle)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
