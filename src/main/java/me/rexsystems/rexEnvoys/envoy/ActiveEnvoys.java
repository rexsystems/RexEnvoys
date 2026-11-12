package me.rexsystems.rexEnvoys.envoy;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ActiveEnvoys {

    private final Set<String> ids = ConcurrentHashMap.newKeySet();

    public boolean add(String id) {
        return ids.add(id);
    }

    public boolean remove(String id) {
        return ids.remove(id);
    }

    public boolean contains(String id) {
        return id != null && ids.contains(id);
    }

    public Set<String> view() {
        return Set.copyOf(ids);
    }
}
