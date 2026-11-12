package me.rexsystems.rexEnvoys.loot;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class LootRollTest {

    @Test
    void secondCallReturnsTheSameListWithoutRerolling() {
        ScriptedRandom random = new ScriptedRandom(0.0, 0.99);
        LootEntry entry = new LootEntry(40, List.of("give {player} diamond 1"), List.of(new LootItem("DIAMOND", 1, "", List.of())));
        LootRoll roll = new LootRoll(List.of(entry), random);

        List<LootEntry> first = roll.items();
        int used = random.reads();
        List<LootEntry> second = roll.items();

        assertSame(first, second);
        assertEquals(first, second);
        assertEquals(List.of(entry), first);
        assertEquals(1, used);
        assertEquals(used, random.reads());
    }

    private static final class ScriptedRandom extends Random {
        private final Queue<Double> values = new ArrayDeque<>();
        private int reads;

        private ScriptedRandom(double... scripted) {
            super(1L);
            for (double value : scripted) {
                values.add(value);
            }
        }

        @Override
        public double nextDouble() {
            reads++;
            Double next = values.poll();
            return next == null ? 0.0 : next;
        }

        private int reads() {
            return reads;
        }
    }
}
