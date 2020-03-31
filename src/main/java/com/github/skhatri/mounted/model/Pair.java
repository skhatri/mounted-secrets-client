package com.github.skhatri.mounted.model;

public final class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K k, V v) {
        this.key = k;
        this.value = v;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
