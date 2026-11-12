package me.rexsystems.rexEnvoys.flare;

import me.rexsystems.rexEnvoys.RexEnvoys;
import me.rexsystems.rexEnvoys.envoy.EnvoyDefinition;
import me.rexsystems.rexEnvoys.util.SchedulerUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class FlareListener implements Listener {

    private final RexEnvoys plugin;

    public FlareListener(RexEnvoys plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onUse(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        String eventId = FlareItems.eventId(plugin, item);
        if (eventId == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand() == null ? EquipmentSlot.HAND : event.getHand();
        Runnable consume = () -> SchedulerUtils.runForPlayer(plugin, player, () -> takeOne(player, hand, eventId));
        plugin.envoys().call(player, eventId, consume);
    }

    private void takeOne(Player player, EquipmentSlot hand, String eventId) {
        if (!player.isOnline() || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        ItemStack current = hand == EquipmentSlot.OFF_HAND
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();
        if (!eventId.equals(FlareItems.eventId(plugin, current))) {
            return;
        }
        EnvoyDefinition definition = plugin.catalog().find(eventId);
        if (definition != null && !definition.flare().consume()) {
            return;
        }
        if (current.getAmount() <= 1) {
            if (hand == EquipmentSlot.OFF_HAND) {
                player.getInventory().setItemInOffHand(null);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            return;
        }
        current.setAmount(current.getAmount() - 1);
    }
}
