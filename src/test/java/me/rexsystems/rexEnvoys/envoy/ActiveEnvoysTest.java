package me.rexsystems.rexEnvoys.envoy;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActiveEnvoysTest {

    @Test
    void twoEventIdsCanBeActiveTogether() {
        ActiveEnvoys active = new ActiveEnvoys();
        assertTrue(active.add("supply"));
        assertTrue(active.add("airdrop"));

        assertTrue(active.contains("supply"));
        assertTrue(active.contains("airdrop"));
        assertEquals(Set.of("supply", "airdrop"), active.view());
    }
}
