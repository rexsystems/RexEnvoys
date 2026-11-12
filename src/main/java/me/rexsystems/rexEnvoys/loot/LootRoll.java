package me.rexsystems.rexEnvoys.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class LootRoll {

    private final List<LootEntry> entries;
    private final Random random;
    private volatile List<LootEntry> rolled;

    public LootRoll(List<LootEntry> entries, Random random) {
        this.entries = List.copyOf(entries);
        this.random = random;
    }

    public List<LootEntry> items() {
        List<LootEntry> existing = rolled;
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            if (rolled != null) {
                return rolled;
            }
            List<LootEntry> built = new ArrayList<>();
            for (LootEntry entry : entries) {
                if (hits(entry.chance())) {
                    built.add(entry);
                }
            }
            // Cached so a later call returns this list and does not roll again.
            rolled = List.copyOf(built);
            return rolled;
        }
    }

    private boolean hits(double chance) {
        if (chance >= 100.0) {
            return true;
        }
        if (chance <= 0.0) {
            return false;
        }
        return random.nextDouble() * 100.0 < chance;
    }
}
