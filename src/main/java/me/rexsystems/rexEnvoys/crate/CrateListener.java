package me.rexsystems.rexEnvoys.crate;

import me.rexsystems.rexEnvoys.RexEnvoys;
import me.rexsystems.rexEnvoys.util.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public final class CrateListener implements Listener {

    private final RexEnvoys plugin;

    public CrateListener(RexEnvoys plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        var crate = plugin.envoys().crateAt(event.getClickedBlock().getLocation());
        if (crate == null) {
            return;
        }
        plugin.envoys().ensureLoot(crate, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {
        watchNow(event.getInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        watchLater(event.getView().getTopInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        watchLater(event.getView().getTopInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(InventoryMoveItemEvent event) {
        watchLater(event.getSource());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        plugin.envoys().broken(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            plugin.envoys().broken(block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            plugin.envoys().broken(block.getLocation());
        }
    }

    private void watchNow(Inventory inventory) {
        Location location = inventory.getLocation();
        if (!ours(location)) {
            return;
        }
        SchedulerUtils.runAtLocation(plugin, location, () -> plugin.envoys().removeIfEmpty(location));
    }

    private void watchLater(Inventory inventory) {
        Location location = inventory.getLocation();
        if (!ours(location)) {
            return;
        }
        Bukkit.getRegionScheduler().runDelayed(plugin, location, task -> plugin.envoys().removeIfEmpty(location), 1L);
    }

    private boolean ours(Location location) {
        return location != null && location.getWorld() != null && plugin.envoys().crateAt(location) != null;
    }
}
