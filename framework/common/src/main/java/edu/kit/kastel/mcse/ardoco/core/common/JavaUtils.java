/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

public final class JavaUtils {

    private JavaUtils() {
        throw new IllegalAccessError();
    }

    public static <K, V> Map<K, V> copyMap(Map<K, V> map, UnaryOperator<V> copy) {
        Map<K, V> copyMap = new HashMap<>();
        for (var entry : map.entrySet()) {
            copyMap.put(entry.getKey(), copy.apply(entry.getValue()));
        }
        return copyMap;
    }

    public static <K, V> Map<V, Set<K>> reverseMap(Map<K, V> map) {
        Map<V, Set<K>> result = new HashMap<>();
        for (var entry : map.entrySet()) {
            result.computeIfAbsent(entry.getValue(), k -> new HashSet<>()).add(entry.getKey());
        }
        return result;
    }

}
