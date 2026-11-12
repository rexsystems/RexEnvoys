package me.rexsystems.rexEnvoys.spawn;

public final class SafeSpawn {

    private SafeSpawn() {
    }

    public static boolean allowed(int y, int worldMinY, boolean liquid, boolean solidGround, boolean safeSpawn) {
        if (!safeSpawn) {
            return true;
        }
        if (y < worldMinY) {
            return false;
        }
        if (liquid) {
            return false;
        }
        return solidGround;
    }
}
