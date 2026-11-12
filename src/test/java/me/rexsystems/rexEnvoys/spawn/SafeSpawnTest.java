package me.rexsystems.rexEnvoys.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeSpawnTest {

    @Test
    void rejectsLiquid() {
        assertFalse(SafeSpawn.allowed(70, -64, true, true, true));
    }

    @Test
    void rejectsVoidBelowWorldMin() {
        assertFalse(SafeSpawn.allowed(-65, -64, false, true, true));
    }

    @Test
    void rejectsGroundThatIsNotSolid() {
        assertFalse(SafeSpawn.allowed(70, -64, false, false, true));
    }

    @Test
    void acceptsASolidSurfaceAboveTheVoid() {
        assertTrue(SafeSpawn.allowed(70, -64, false, true, true));
    }
}
