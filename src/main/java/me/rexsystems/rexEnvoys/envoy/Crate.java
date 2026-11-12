package me.rexsystems.rexEnvoys.envoy;

import me.rexsystems.rexEnvoys.loot.LootEntry;
import me.rexsystems.rexEnvoys.loot.LootRoll;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Crate {

    private final UUID id = UUID.randomUUID();
    private final String eventId;
    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final LootRoll roll;
    private final boolean despawnWhenEmpty;
    private final String callerName;
    private final Set<UUID> openers = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean filled = new AtomicBoolean();

    public Crate(String eventId, String world, int x, int y, int z, LootRoll roll, boolean despawnWhenEmpty, String callerName) {
        this.eventId = eventId;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.roll = roll;
        this.despawnWhenEmpty = despawnWhenEmpty;
        this.callerName = callerName == null ? "" : callerName;
    }

    public UUID id() {
        return id;
    }

    public String eventId() {
        return eventId;
    }

    public String world() {
        return world;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public boolean despawnWhenEmpty() {
        return despawnWhenEmpty;
    }

    public String callerName() {
        return callerName;
    }

    public String posKey() {
        return world + ":" + x + ":" + y + ":" + z;
    }

    public boolean fillOnce() {
        return filled.compareAndSet(false, true);
    }

    public boolean filled() {
        return filled.get();
    }

    public List<LootEntry> loot() {
        return roll.items();
    }

    public boolean markOpened(UUID player) {
        return player != null && openers.add(player);
    }

    public Location toLocation() {
        World found = Bukkit.getWorld(world);
        if (found == null) {
            return null;
        }
        return new Location(found, x, y, z);
    }
}
