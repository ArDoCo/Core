/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.collection;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Unmodifiable view of {@link LinkedHashMap}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@Deterministic
public class UnmodifiableLinkedHashMap<K, V> implements Map<K, V>, Serializable {
    private final Map<K, V> map;

    public UnmodifiableLinkedHashMap(LinkedHashMap<K, V> map) {
        //This does not affect iteration order
        this.map = Collections.unmodifiableMap(map);
    }

    public static <K, V> UnmodifiableLinkedHashMap<K, V> of(LinkedHashMap<K, V> map) {
        return new UnmodifiableLinkedHashMap<>(map);
    }

    public static <K, V> UnmodifiableLinkedHashMap<K, V> of(Stream<Entry<K,V>> entryStream) {
        var map = new LinkedHashMap<K, V>();
        entryStream.forEachOrdered(e -> map.put(e.getKey(), e.getValue()));
        return new UnmodifiableLinkedHashMap<>(map);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public UnmodifiableLinkedHashSet<K> keySet() {
        return new UnmodifiableLinkedHashSet<>(map.keySet());
    }

    @NotNull
    @Override
    public List<V> values() {
        return map.values().stream().toList();
    }

    @NotNull
    @Override
    public UnmodifiableLinkedHashSet<Entry<K, V>> entrySet() {
        //This is a Collections$LinkedEntrySet and guarantees iteration order as well.
        return new UnmodifiableLinkedHashSet<>(map.entrySet());
    }
}
