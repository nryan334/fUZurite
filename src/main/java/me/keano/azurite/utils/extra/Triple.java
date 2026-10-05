package me.keano.azurite.utils.extra;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class Triple<F, S, T> {

    private final Map<Pair<F, S>, T> map;

    public Triple() {
        this.map = new ConcurrentHashMap<>();
    }

    public Triple(Map<Pair<F, S>, T> map) {
        this.map = map;
    }

    public boolean contains(F first, S second) {
        return map.containsKey(new Pair<>(first, second));
    }

    public T get(F first, S second) {
        return map.get(new Pair<>(first, second));
    }

    public T remove(F first, S second) {
        return map.remove(new Pair<>(first, second));
    }

    public void removeFirst(F first) {
        map.keySet().removeIf(pair -> pair.getKey().equals(first));
    }

    public void put(F first, S second, T third) {
        map.put(new Pair<>(first, second), third);
    }

    public Collection<T> values() {
        return map.values();
    }

    public void clear() {
        map.clear();
    }
}