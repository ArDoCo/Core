package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.util.Collection;

/**
 * Classes implementing this interface provide the functionality to determine whether an instance is similar to the provided type, and if collections of
 * instances are similar to collections of the type.
 *
 * @param <T> the type
 */
public interface SimilarityComparable<T> {
    /**
     * {@return whether the instance is similar to the given object} Has to return true if {@link Object#equals} returns true. The result of this function
     * should be symmetric, but does not have to be transitive.
     *
     * @param obj some object
     */
    boolean similar(T obj);

    /**
     * {@return both collections consist of elements, that have a corresponding similar element in the other collection} Does not care about order and should
     * returns true for equal lists if {@link #similar(Object)} was implemented correctly.
     *
     * @param a some collection
     * @param b some other collection
     */
    static <T extends SimilarityComparable<T>> boolean similar(Collection<? extends T> a, Collection<? extends T> b) {
        if (a.equals(b))
            return true;
        if (a.size() != b.size())
            return false;
        return a.parallelStream().allMatch(element -> b.stream().anyMatch(element::similar));
    }
}
