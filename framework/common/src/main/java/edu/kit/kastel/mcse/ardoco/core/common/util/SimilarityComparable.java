/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.util.Collection;

import edu.kit.kastel.mcse.ardoco.core.data.MetaData;

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
     * @param metaData the pipeline meta data containing the similarity configuration
     * @param obj      some object
     */
    boolean similar(MetaData metaData, T obj);

    /**
     * {@return both collections consist of elements, that have a corresponding similar element in the other collection} Does not care about order and should
     * returns true for equal lists if {@link #similar(MetaData, Object)} was implemented correctly.
     *
     * @param metaData the pipeline meta data containing the similarity configuration
     * @param a        some collection
     * @param b        some other collection
     */
    static <T extends SimilarityComparable<T>> boolean similar(MetaData metaData, Collection<? extends T> a, Collection<? extends T> b) {
        if (a.equals(b))
            return true;
        if (a.size() != b.size())
            return false;
        return a.parallelStream().allMatch(element -> b.stream().anyMatch(other -> element.similar(metaData, other)));
    }
}