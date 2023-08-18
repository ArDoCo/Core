/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.util.Collection;

import org.eclipse.collections.api.collection.ImmutableCollection;

public final class Comparators {
    private Comparators() {
        throw new IllegalAccessError();
    }

    public static <T> boolean collectionsEqualsAnyOrder(ImmutableCollection<T> first, ImmutableCollection<T> second) {
        return collectionsEqualsAnyOrder(first.castToCollection(), second.castToCollection());
    }

    public static <T> boolean collectionsEqualsAnyOrder(Collection<T> first, Collection<T> second) {
        return first.size() == second.size() && first.containsAll(second) && second.containsAll(first);
    }

    public static <T> boolean collectionsIdentityAnyOrder(ImmutableCollection<T> first, ImmutableCollection<T> second) {
        return collectionsIdentityAnyOrder(first.castToCollection(), second.castToCollection());
    }

    public static <T> boolean collectionsIdentityAnyOrder(Collection<T> first, Collection<T> second) {
        return first.size() == second.size() && first.stream().allMatch(f -> second.stream().anyMatch(s -> f == s));
    }
}
