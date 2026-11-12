package me.rexsystems.rexEnvoys.spawn;

import org.bukkit.World;
import org.bukkit.block.Block;

public final class BukkitGround {

    private BukkitGround() {
    }

    public static Ground at(World world, int x, int y, int z) {
        int min = world.getMinHeight();
        int max = world.getMaxHeight();
        if (y < min || y >= max) {
            return new Ground(false, false);
        }
        Block at = world.getBlockAt(x, y, z);
        boolean liquid = at.isLiquid();
        boolean solidGround = false;
        if (y - 1 >= min) {
            Block below = world.getBlockAt(x, y - 1, z);
            liquid = liquid || below.isLiquid();
            solidGround = below.getType().isSolid() && !below.isLiquid() && !at.getType().isSolid() && !at.isLiquid();
        }
        return new Ground(liquid, solidGround);
    }
}
