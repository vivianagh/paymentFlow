package com.paymentflow.service.learning;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//Educational cache using DCL idea with volatile holder per key
// For production: prefer Caffeine or ConcurrentHashMap.computeIfAbsent directly
public class ManualCache<K, V> {

    private static final class Holder<V> {
        volatile V value; //volatile ensures visibility for DCL
    }

    private final Map<K, Holder<V>> cache = new ConcurrentHashMap<>();

    public V remember(K key, ValueProvider<K, V> provider) {
        Holder<V> holder = cache.computeIfAbsent(key, k -> new Holder<>());
        V v = holder.value;
        if (v != null) return v;

        synchronized (holder) {
            v = holder.value;
            if (v == null) {
                v = provider.load(key);
                holder.value = v;
            }
        }
        return v;
    }
    @FunctionalInterface
    public interface ValueProvider<K, V> {
        V load(K key);
    }
}
