package me.rexsystems.rexEnvoys.envoy;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.rexsystems.rexEnvoys.Keys;
import me.rexsystems.rexEnvoys.RexEnvoys;
import me.rexsystems.rexEnvoys.block.CustomBlockPlacer;
import me.rexsystems.rexEnvoys.loot.LootEntry;
import me.rexsystems.rexEnvoys.loot.LootRoll;
import me.rexsystems.rexEnvoys.loot.LootStacks;
import me.rexsystems.rexEnvoys.spawn.BukkitGround;
import me.rexsystems.rexEnvoys.spawn.Ground;
import me.rexsystems.rexEnvoys.spawn.SafeSpawn;
import me.rexsystems.rexEnvoys.util.MessageService;
import me.rexsystems.rexEnvoys.util.Placeholders;
import me.rexsystems.rexEnvoys.util.SchedulerUtils;
import me.rexsystems.rexEnvoys.util.Sounds;
import me.rexsystems.rexEnvoys.util.TextParser;
import me.rexsystems.rexEnvoys.util.Tokens;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class EnvoyService {

    private static final Set<Material> CONTAINERS = Set.of(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL);

    private final RexEnvoys plugin;
    private final Object lock = new Object();
    private final ActiveEnvoys active = new ActiveEnvoys();
    private final Map<UUID, Crate> crates = new ConcurrentHashMap<>();
    private final Map<String, UUID> locations = new ConcurrentHashMap<>();
    private final Map<String, Long> nextRun = new ConcurrentHashMap<>();
    private final Set<String> logged = ConcurrentHashMap.newKeySet();
    private ScheduledTask ticker;

    public EnvoyService(RexEnvoys plugin) {
        this.plugin = plugin;
    }

    public ActiveEnvoys active() {
        return active;
    }

    public void startTicker() {
        ticker = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> tickSchedules(), 20L, 20L);
    }

    public void shutdown() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        List<Crate> open;
        synchronized (lock) {
            open = new ArrayList<>(crates.values());
            crates.clear();
            locations.clear();
            for (String id : active.view()) {
                active.remove(id);
            }
        }
        for (Crate crate : open) {
            Location location = crate.toLocation();
            if (location == null) {
                continue;
            }
            try {
                clearBlock(location, crate.id());
            } catch (Throwable ignored) {
                SchedulerUtils.runAtLocation(plugin, location, () -> clearBlock(location, crate.id()));
            }
        }
    }

    public void start(CommandSender sender, String rawId) {
        EnvoyDefinition definition = plugin.catalog().find(rawId);
        if (definition == null) {
            feedback(sender, "unknown-event", Map.of("id", rawId == null ? "" : rawId));
            return;
        }
        if (!definition.enabled()) {
            feedback(sender, "disabled", Map.of("id", definition.id()));
            return;
        }
        if (!begin(definition)) {
            feedback(sender, "already-running", Map.of("id", definition.id()));
            return;
        }
        String caller = sender instanceof Player player ? player.getName() : "";
        launch(definition, sender, caller, false, null);
    }

    public void call(Player player, String rawId, Runnable consume) {
        EnvoyDefinition definition = plugin.catalog().find(rawId);
        if (definition == null) {
            feedback(player, "unknown-event", Map.of("id", rawId == null ? "" : rawId));
            return;
        }
        if (!definition.enabled()) {
            feedback(player, "disabled", Map.of("id", definition.id()));
            return;
        }
        if (!begin(definition)) {
            feedback(player, "already-running", Map.of("id", definition.id()));
            return;
        }
        launch(definition, player, player.getName(), true, consume);
    }

    public void stop(CommandSender sender, String rawId) {
        EnvoyDefinition definition = plugin.catalog().find(rawId);
        if (definition == null) {
            feedback(sender, "unknown-event", Map.of("id", rawId == null ? "" : rawId));
            return;
        }
        List<Crate> removed = new ArrayList<>();
        boolean wasActive;
        synchronized (lock) {
            wasActive = active.remove(definition.id());
            Iterator<Crate> iterator = crates.values().iterator();
            while (iterator.hasNext()) {
                Crate crate = iterator.next();
                if (!definition.id().equals(crate.eventId())) {
                    continue;
                }
                iterator.remove();
                locations.remove(crate.posKey());
                removed.add(crate);
            }
        }
        if (!wasActive && removed.isEmpty()) {
            feedback(sender, "not-running", Map.of("id", definition.id()));
            return;
        }
        for (Crate crate : removed) {
            Location location = crate.toLocation();
            if (location == null) {
                continue;
            }
            SchedulerUtils.runAtLocation(plugin, location, () -> clearBlock(location, crate.id()));
        }
        feedback(sender, "stopped", Map.of("id", definition.id()));
    }

    public Crate crateAt(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        String key = key(location);
        synchronized (lock) {
            UUID id = locations.get(key);
            return id == null ? null : crates.get(id);
        }
    }

    public void ensureLoot(Crate crate, Player player) {
        if (crate == null) {
            return;
        }
        boolean wrote = false;
        if (crate.fillOnce()) {
            writeLoot(crate);
            runCommands(crate.loot(), tokens(crate, player));
            wrote = true;
        }
        if (player != null && crate.markOpened(player.getUniqueId())) {
            EnvoyDefinition definition = plugin.catalog().find(crate.eventId());
            if (definition != null) {
                String text = Placeholders.apply(player, Tokens.apply(definition.messages().open(), tokens(crate, player)));
                MessageService.send(player, text);
            }
        }
        if (wrote && crate.despawnWhenEmpty()) {
            Location location = crate.toLocation();
            if (location != null) {
                SchedulerUtils.runAtLocation(plugin, location, () -> removeIfEmpty(location));
            }
        }
    }

    public void removeIfEmpty(Location location) {
        Crate crate = crateAt(location);
        if (crate == null || !crate.despawnWhenEmpty() || !crate.filled() || hasItems(location)) {
            return;
        }
        SchedulerUtils.runAtLocation(plugin, location, () -> deleteFromRegionThread(crate.id(), Reason.EMPTY));
    }

    public void broken(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        Crate crate;
        synchronized (lock) {
            UUID id = locations.remove(key(location));
            crate = id == null ? null : crates.remove(id);
        }
        if (crate == null) {
            return;
        }
        SchedulerUtils.runAtLocation(plugin, location, () -> removeMarker(location, crate.id()));
        finishIfIdle(crate.eventId());
    }

    private void tickSchedules() {
        long now = System.currentTimeMillis();
        for (EnvoyDefinition definition : plugin.catalog().all()) {
            if (!definition.enabled() || !definition.scheduled()) {
                continue;
            }
            long due = nextRun.computeIfAbsent(definition.id(), id -> now + definition.intervalMillis());
            if (now < due) {
                continue;
            }
            synchronized (lock) {
                if (active.contains(definition.id())) {
                    nextRun.put(definition.id(), now + definition.intervalMillis());
                    continue;
                }
                if (!active.add(definition.id())) {
                    continue;
                }
            }
            nextRun.put(definition.id(), now + definition.intervalMillis());
            launch(definition, null, "", false, null);
        }
    }

    private boolean begin(EnvoyDefinition definition) {
        synchronized (lock) {
            return active.add(definition.id());
        }
    }

    private void launch(EnvoyDefinition definition, CommandSender actor, String caller, boolean fromFlare, Runnable consume) {
        if (definition.fixed()) {
            if (definition.points().isEmpty()) {
                abandon(definition, actor);
                return;
            }
            Wave wave = new Wave(definition, actor, fromFlare, consume, definition.points().size());
            for (EnvoyDefinition.FixedPoint point : definition.points()) {
                World world = Bukkit.getWorld(point.world());
                if (world == null) {
                    warnOnce("world:" + point.world(), "World '" + point.world() + "' is not loaded. That crate was skipped.");
                    wave.skipped();
                    continue;
                }
                Location location = new Location(world, point.x(), point.y(), point.z());
                SchedulerUtils.runAtLocation(plugin, location, () -> {
                    if (!active.contains(definition.id())) {
                        wave.skipped();
                        return;
                    }
                    PlaceResult result = place(definition, wave, world, point.x(), point.y(), point.z(), caller);
                    if (result != PlaceResult.PLACED) {
                        wave.skipped();
                    }
                });
            }
            return;
        }

        World world = Bukkit.getWorld(definition.region().world());
        int amount = definition.amount();
        if (world == null || amount <= 0) {
            if (world == null && amount > 0) {
                warnOnce("world:" + definition.region().world(), "World '" + definition.region().world() + "' is not loaded. That crate was skipped.");
            }
            abandon(definition, actor);
            return;
        }
        Wave wave = new Wave(definition, actor, fromFlare, consume, amount);
        int attempts = plugin.config().spawnAttempts();
        for (int i = 0; i < amount; i++) {
            seek(definition, wave, attempts, world, caller);
        }
    }

    private void seek(EnvoyDefinition definition, Wave wave, int left, World world, String caller) {
        if (left <= 0 || world == null) {
            wave.skipped();
            return;
        }
        EnvoyDefinition.Region region = definition.region();
        int x = range(region.minX(), region.maxX());
        int y = range(region.minY(), region.maxY());
        int z = range(region.minZ(), region.maxZ());
        Location probe = new Location(world, x, y, z);
        SchedulerUtils.runAtLocation(plugin, probe, () -> {
            if (!active.contains(definition.id())) {
                wave.skipped();
                return;
            }
            Ground ground = BukkitGround.at(world, x, y, z);
            boolean safe = plugin.config().safeSpawn();
            if (!SafeSpawn.allowed(y, world.getMinHeight(), ground.liquid(), ground.solidGround(), safe)) {
                seek(definition, wave, left - 1, world, caller);
                return;
            }
            PlaceResult result = place(definition, wave, world, x, y, z, caller);
            if (result == PlaceResult.PLACED) {
                return;
            }
            if (result == PlaceResult.ABORT) {
                wave.skipped();
                return;
            }
            seek(definition, wave, left - 1, world, caller);
        });
    }

    private PlaceResult place(EnvoyDefinition definition, Wave wave, World world, int x, int y, int z, String caller) {
        String key = world.getName() + ":" + x + ":" + y + ":" + z;
        Crate crate = new Crate(
                definition.id(),
                world.getName(),
                x,
                y,
                z,
                new LootRoll(definition.loot(), new java.util.Random()),
                definition.despawnWhenEmpty(),
                caller
        );
        synchronized (lock) {
            if (!active.contains(definition.id())) {
                return PlaceResult.ABORT;
            }
            if (locations.containsKey(key)) {
                return PlaceResult.RETRY;
            }
            crates.put(crate.id(), crate);
            locations.put(key, crate.id());
        }
        try {
            Location location = new Location(world, x, y, z);
            putBlock(definition, location);
            spawnMarker(crate, location);
            if (fillsOnLand(definition) && crate.fillOnce()) {
                writeLoot(crate);
                runCommands(crate.loot(), tokens(crate, null));
                if (crate.despawnWhenEmpty()) {
                    SchedulerUtils.runAtLocation(plugin, location, () -> removeIfEmpty(location));
                }
            }
            if (definition.firework()) {
                burst(location);
            }
            playSound(location, definition.sound());
            scheduleTimeout(definition, crate);
        } catch (Throwable failure) {
            plugin.getLogger().warning("Could not place a crate for " + definition.id() + " at "
                    + x + " " + y + " " + z + ": " + failure.getMessage());
            synchronized (lock) {
                crates.remove(crate.id());
                locations.remove(key);
            }
            try {
                world.getBlockAt(x, y, z).setType(Material.AIR, false);
            } catch (Throwable ignored) {
            }
            return PlaceResult.RETRY;
        }
        wave.placed(crate);
        return PlaceResult.PLACED;
    }

    private void putBlock(EnvoyDefinition definition, Location location) {
        Material material = definition.material();
        if (!CONTAINERS.contains(material)) {
            warnOnce("material:" + material.name(), material.name() + " cannot hold crate loot. Using CHEST.");
            material = Material.CHEST;
        }
        CustomBlockPlacer.place(plugin, location.getBlock(), definition.customBlockId(), material);
        if (!(location.getBlock().getState() instanceof Container)) {
            warnOnce("not-container:" + definition.customBlockId(),
                    "Custom block '" + definition.customBlockId() + "' is not a container. Using " + material.name() + ".");
            location.getBlock().setType(material, false);
        }
    }

    private void spawnMarker(Crate crate, Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        Location standAt = location.clone().add(0.5, 0, 0.5);
        world.spawn(standAt, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setPersistent(true);
            stand.setCollidable(false);
            stand.setBasePlate(false);
            stand.setRemoveWhenFarAway(false);
            stand.getPersistentDataContainer().set(Keys.crate(plugin), PersistentDataType.STRING, crate.id().toString());
        });
    }

    private void burst(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        Location at = location.clone().add(0.5, 0.2, 0.5);
        Firework firework = world.spawn(at, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL)
                .withColor(Color.YELLOW)
                .withFade(Color.WHITE)
                .build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);
        firework.detonate();
    }

    private void playSound(Location location, EnvoyDefinition.SoundCue cue) {
        if (cue == null || !cue.enabled() || location.getWorld() == null) {
            return;
        }
        Sound sound = Sounds.resolve(cue.sound());
        if (sound == null) {
            if (cue.sound() != null && !cue.sound().isBlank()) {
                warnOnce("sound:" + cue.sound(), "Unknown sound '" + cue.sound() + "'.");
            }
            return;
        }
        location.getWorld().playSound(location, sound, cue.volume(), cue.pitch());
    }

    private void scheduleTimeout(EnvoyDefinition definition, Crate crate) {
        if (definition.timeoutSeconds() <= 0) {
            return;
        }
        long ticks = definition.timeoutSeconds() * 20L;
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
            Location location = crate.toLocation();
            if (location == null) {
                deleteFromRegionThread(crate.id(), Reason.TIMEOUT);
                return;
            }
            SchedulerUtils.runAtLocation(plugin, location, () -> deleteFromRegionThread(crate.id(), Reason.TIMEOUT));
        }, ticks);
    }

    private void writeLoot(Crate crate) {
        Location location = crate.toLocation();
        if (location == null) {
            return;
        }
        Block block = location.getBlock();
        BlockState state = block.getState();
        if (!(state instanceof Container container)) {
            return;
        }
        Inventory inventory = container.getInventory();
        inventory.clear();
        List<ItemStack> stacks = LootStacks.from(crate.loot());
        int slot = 0;
        for (ItemStack stack : stacks) {
            if (slot >= inventory.getSize()) {
                World world = location.getWorld();
                if (world != null) {
                    world.dropItemNaturally(location.clone().add(0.5, 0.5, 0.5), stack);
                }
                continue;
            }
            inventory.setItem(slot++, stack);
        }
    }

    private void runCommands(List<LootEntry> rolled, Map<String, String> tokens) {
        List<String> commands = new ArrayList<>();
        for (LootEntry entry : rolled) {
            for (String command : entry.commands()) {
                if (command == null || command.isBlank()) {
                    continue;
                }
                String line = command.replace('\n', ' ').replace('\r', ' ').trim();
                for (Map.Entry<String, String> token : tokens.entrySet()) {
                    String value = token.getValue() == null ? "" : token.getValue();
                    line = line.replace("{" + token.getKey() + "}", value);
                }
                if (!line.isBlank()) {
                    commands.add(line);
                }
            }
        }
        if (commands.isEmpty()) {
            return;
        }
        SchedulerUtils.runSync(plugin, () -> {
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        });
    }

    private boolean fillsOnLand(EnvoyDefinition definition) {
        String mode = definition.lootWhen();
        if (mode == null || mode.isBlank()) {
            mode = plugin.config().lootWhen();
        }
        return "land".equalsIgnoreCase(mode);
    }

    private void deleteFromRegionThread(UUID id, Reason reason) {
        Crate crate;
        synchronized (lock) {
            crate = crates.remove(id);
            if (crate != null) {
                locations.remove(crate.posKey());
            }
        }
        if (crate == null) {
            return;
        }
        Location location = crate.toLocation();
        if (location != null) {
            clearBlock(location, id);
        }
        EnvoyDefinition definition = plugin.catalog().find(crate.eventId());
        if (definition != null) {
            Map<String, String> tokens = tokens(crate, null);
            if (reason == Reason.TIMEOUT) {
                tellWorld(crate.world(), definition.messages().timeout(), tokens);
            } else if (reason == Reason.EMPTY) {
                tellWorld(crate.world(), definition.messages().empty(), tokens);
            }
        }
        finishIfIdle(crate.eventId());
    }

    // Block and marker entity are removed on the region scheduler.
    private void clearBlock(Location location, UUID crateId) {
        Block block = location.getBlock();
        BlockState state = block.getState();
        if (state instanceof Container container) {
            container.getInventory().clear();
        }
        block.setType(Material.AIR, false);
        removeMarker(location, crateId);
    }

    private void removeMarker(Location location, UUID crateId) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        String tag = crateId.toString();
        Location center = location.clone().add(0.5, 0.5, 0.5);
        for (Entity entity : world.getNearbyEntities(center, 1.25, 1.5, 1.25)) {
            String value = entity.getPersistentDataContainer().get(Keys.crate(plugin), PersistentDataType.STRING);
            if (tag.equals(value)) {
                entity.remove();
            }
        }
    }

    private boolean hasItems(Location location) {
        BlockState state = location.getBlock().getState();
        if (!(state instanceof Container container)) {
            return false;
        }
        for (ItemStack stack : container.getInventory().getContents()) {
            if (stack != null && !stack.getType().isAir()) {
                return true;
            }
        }
        return false;
    }

    private void finishIfIdle(String eventId) {
        synchronized (lock) {
            for (Crate crate : crates.values()) {
                if (eventId.equals(crate.eventId())) {
                    return;
                }
            }
            active.remove(eventId);
        }
    }

    private void abandon(EnvoyDefinition definition, CommandSender actor) {
        boolean tell;
        synchronized (lock) {
            tell = count(definition.id()) == 0 && active.remove(definition.id());
        }
        if (!tell) {
            return;
        }
        if (actor == null) {
            plugin.getLogger().warning("No crate could be placed for " + definition.id() + ".");
            return;
        }
        feedback(actor, "spawn-failed", Map.of("id", definition.id()));
    }

    private int count(String eventId) {
        int found = 0;
        for (Crate crate : crates.values()) {
            if (eventId.equals(crate.eventId())) {
                found++;
            }
        }
        return found;
    }

    private void tellWorld(String worldName, String template, Map<String, String> tokens) {
        if (worldName == null || template == null || template.isBlank()) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            SchedulerUtils.runForPlayer(plugin, player, () -> {
                if (!player.isOnline() || !worldName.equals(player.getWorld().getName())) {
                    return;
                }
                String text = Placeholders.apply(player, Tokens.apply(template, tokens));
                MessageService.send(player, text);
            });
        }
    }

    private void showActionBar(String worldName, EnvoyDefinition.TextLine line, Map<String, String> tokens) {
        if (worldName == null || line == null || !line.enabled()) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            SchedulerUtils.runForPlayer(plugin, player, () -> {
                if (!player.isOnline() || !worldName.equals(player.getWorld().getName())) {
                    return;
                }
                String text = Placeholders.apply(player, Tokens.apply(line.text(), tokens));
                MessageService.actionBar(player, text, Map.of());
            });
        }
    }

    private void showBossBar(String worldName, EnvoyDefinition.BossBarLine line, Map<String, String> tokens) {
        if (worldName == null || line == null || !line.enabled() || line.text() == null || line.text().isBlank()) {
            return;
        }
        BossBar.Color color = bossColor(line.color());
        BossBar.Overlay overlay = bossOverlay(line.style());
        long ticks = Math.max(1, Math.min(60, line.seconds())) * 20L;
        for (Player player : Bukkit.getOnlinePlayers()) {
            SchedulerUtils.runForPlayer(plugin, player, () -> {
                if (!player.isOnline() || !worldName.equals(player.getWorld().getName())) {
                    return;
                }
                Component title = TextParser.parse(Placeholders.apply(player, Tokens.apply(line.text(), tokens)));
                BossBar bar = BossBar.bossBar(title, 1.0f, color, overlay);
                player.showBossBar(bar);
                SchedulerUtils.runForPlayerLater(plugin, player, () -> player.hideBossBar(bar), ticks);
            });
        }
    }

    private void feedback(CommandSender sender, String path, Map<String, String> extra) {
        if (sender == null) {
            return;
        }
        Map<String, String> tokens = new HashMap<>();
        if (extra != null) {
            tokens.putAll(extra);
        }
        tokens.put("prefix", plugin.config().prefix());
        String text = Tokens.apply(plugin.config().raw(path), tokens);
        if (sender instanceof Player player) {
            SchedulerUtils.runForPlayer(plugin, player, () -> MessageService.send(player, Placeholders.apply(player, text)));
            return;
        }
        SchedulerUtils.runSync(plugin, () -> MessageService.send(sender, text));
    }

    private Map<String, String> tokens(Crate crate, Player player) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("prefix", plugin.config().prefix());
        tokens.put("id", crate.eventId());
        tokens.put("player", player != null ? player.getName() : crate.callerName());
        tokens.put("world", crate.world());
        tokens.put("x", Integer.toString(crate.x()));
        tokens.put("y", Integer.toString(crate.y()));
        tokens.put("z", Integer.toString(crate.z()));
        return tokens;
    }

    private void warnOnce(String key, String message) {
        if (logged.add(key)) {
            plugin.getLogger().warning(message);
        }
    }

    private static int range(int min, int max) {
        int low = Math.min(min, max);
        int high = Math.max(min, max);
        if (low == high) {
            return low;
        }
        return low + ThreadLocalRandom.current().nextInt(high - low + 1);
    }

    private static String key(Location location) {
        return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    private static BossBar.Color bossColor(String name) {
        if (name == null || name.isBlank()) {
            return BossBar.Color.YELLOW;
        }
        try {
            return BossBar.Color.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return BossBar.Color.YELLOW;
        }
    }

    private static BossBar.Overlay bossOverlay(String style) {
        if (style == null || style.isBlank()) {
            return BossBar.Overlay.PROGRESS;
        }
        String name = switch (style.trim().toUpperCase(Locale.ROOT)) {
            case "SOLID", "PROGRESS" -> "PROGRESS";
            case "SEGMENTED_6", "NOTCHED_6" -> "NOTCHED_6";
            case "SEGMENTED_10", "NOTCHED_10" -> "NOTCHED_10";
            case "SEGMENTED_12", "NOTCHED_12" -> "NOTCHED_12";
            case "SEGMENTED_20", "NOTCHED_20" -> "NOTCHED_20";
            default -> style.trim().toUpperCase(Locale.ROOT);
        };
        try {
            return BossBar.Overlay.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return BossBar.Overlay.PROGRESS;
        }
    }

    private enum PlaceResult {
        PLACED,
        RETRY,
        ABORT
    }

    private enum Reason {
        EMPTY,
        TIMEOUT
    }

    private final class Wave {
        private final EnvoyDefinition definition;
        private final CommandSender actor;
        private final boolean fromFlare;
        private final Runnable consume;
        private int remaining;
        private int placed;

        private Wave(EnvoyDefinition definition, CommandSender actor, boolean fromFlare, Runnable consume, int remaining) {
            this.definition = definition;
            this.actor = actor;
            this.fromFlare = fromFlare;
            this.consume = consume;
            this.remaining = remaining;
        }

        private void placed(Crate crate) {
            boolean first;
            synchronized (this) {
                placed++;
                remaining--;
                first = placed == 1;
            }
            Map<String, String> tokens = tokens(crate, null);
            if (first) {
                tellWorld(crate.world(), definition.messages().start(), tokens);
                if (fromFlare) {
                    tellWorld(crate.world(), definition.messages().flare(), tokens);
                }
                showActionBar(crate.world(), definition.actionBar(), tokens);
                showBossBar(crate.world(), definition.bossBar(), tokens);
                if (fromFlare) {
                    feedback(actor, "called", Map.of("id", definition.id()));
                } else {
                    feedback(actor, "started", Map.of("id", definition.id()));
                }
                if (consume != null) {
                    consume.run();
                }
            }
            tellWorld(crate.world(), definition.messages().land(), tokens);
        }

        private void skipped() {
            boolean fail;
            synchronized (this) {
                remaining--;
                fail = remaining <= 0 && placed == 0;
            }
            if (fail) {
                abandon(definition, actor);
            }
        }
    }
}
