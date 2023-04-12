/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

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

    public static int similarEntriesOfList(ImmutableList<String> list1, ImmutableList<String> list2) {
        MutableList<String> removed = Lists.mutable.empty();

        for (var element : list1) {
            if (list2.contains(element)) {
                removed.add(element);
            } else {
                if (list2.select(e -> !removed.contains(e) && (e.contains(element) || element.contains(e))).size() == 1) {
                    removed.add(element);
                }
            }
        }

        return removed.size();
    }
}
