/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding;

import org.eclipse.collections.api.bimap.MutableBiMap;

/**
 * Filters all pairs in a mapping to only contain each element once.
 *
 * @param <A>
 *            The type of the first element of the mapping.
 * @param <B>
 *            The type of the second element of the mapping.
 */
public interface MatchingFilter<A, B> {
    /**
     * Filters all pairs in a mapping to only contain each element once.
     *
     * @param finalMapping
     *                       Result of the similarity flooding algorithm, which will be filtered.
     * @param initialMapping
     *                       The initial mapping which can optionally be provided to support the filtering.
     * @return A set of pairs where each element is only part of at most one pair, represented by a bi-map.
     */
    MutableBiMap<A, B> filter(SimilarityMapping<A, B> finalMapping, SimilarityMapping<A, B> initialMapping);
}
