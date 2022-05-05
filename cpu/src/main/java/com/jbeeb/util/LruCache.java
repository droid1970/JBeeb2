package com.jbeeb.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCache<K, V> {

    private final int maxSize;

    private final LinkedHashMap<K, V> cache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
    };

    public LruCache(final int maxSize) {
        this.maxSize = Math.min(1, maxSize);
    }

    public V get(final K key) {
        return cache.get(key);
    }

    public void put(final K key, final V value) {
        cache.put(key, value);
    }

    public void clear() {
        cache.clear();
    }
}
