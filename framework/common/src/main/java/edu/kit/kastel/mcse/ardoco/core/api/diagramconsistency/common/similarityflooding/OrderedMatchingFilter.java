/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;
import org.jgrapht.alg.util.Pair;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * This matching filters orders all pairs by their similarity and assign the pairs with the highest similarity first.
 *
 * @param <A>
 *            The type of the first element in the pairs.
 * @param <B>
 *            The type of the second element in the pairs.
 */
@Deterministic
public class OrderedMatchingFilter<A, B> implements MatchingFilter<A, B> {
    private final double minSimilarity;
    private final double initialSimilarityThreshold;

    /**
     * Creates a new OrderedMatchingFilter.
     *
     * @param minSimilarity
     *                                   The minimum similarity a pair must have to be included in the result.
     * @param initialSimilarityThreshold
     *                                   The minimum similarity two pairs must have to be considered equal in the initial mapping.
     */
    public OrderedMatchingFilter(double minSimilarity, double initialSimilarityThreshold) {
        this.minSimilarity = minSimilarity;
        this.initialSimilarityThreshold = initialSimilarityThreshold;
    }

    /**
     * Creates a new OrderedMatchingFilter. No pairs are rejected because of their similarity.
     */
    public OrderedMatchingFilter() {
        this(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    private static <A, B> void selectBestMappings(List<Assignment<A, B>> sortedAssignments, Set<A> usedElementsInA, Set<B> usedElementsInB,
            MutableBiMap<A, B> filteredPairs) {
        for (Assignment<A, B> assignment : sortedAssignments) {
            if (!usedElementsInA.contains(assignment.pair().getFirst()) && !usedElementsInB.contains(assignment.pair().getSecond())) {
                filteredPairs.put(assignment.pair().getFirst(), assignment.pair().getSecond());
                usedElementsInA.add(assignment.pair().getFirst());
                usedElementsInB.add(assignment.pair().getSecond());
            }
        }
    }

    @Override
    public MutableBiMap<A, B> filter(SimilarityMapping<A, B> finalMapping, SimilarityMapping<A, B> initialMapping) {
        Set<A> usedElementsInA = new LinkedHashSet<>();
        Set<B> usedElementsInB = new LinkedHashSet<>();
        MutableBiMap<A, B> filteredPairs = new HashBiMap<>();

        List<Assignment<A, B>> sortedAssignments = initialMapping == null ? this.sortMappings(finalMapping) : this.sortMappings(finalMapping, initialMapping);
        selectBestMappings(sortedAssignments, usedElementsInA, usedElementsInB, filteredPairs);

        return filteredPairs;
    }

    private List<Assignment<A, B>> sortMappings(SimilarityMapping<A, B> finalMapping) {
        return finalMapping.getMappedElements()
                .stream()
                .map(pair -> new Assignment<>(pair, finalMapping.getSimilarity(pair)))
                .sorted((left, right) -> Double.compare(right.similarity(), left.similarity()))
                .takeWhile(assignment -> assignment.similarity() >= this.minSimilarity)
                .toList();
    }

    private List<Assignment<A, B>> sortMappings(SimilarityMapping<A, B> finalMapping, SimilarityMapping<A, B> initialMapping) {
        Set<Pair<A, B>> allPairs = initialMapping.getMappedElements();
        Set<Pair<A, B>> mappedPairs = finalMapping.getMappedElements();

        List<Assignment<A, B>> assignments = new ArrayList<>();

        for (Pair<A, B> pair : allPairs) {
            if (mappedPairs.contains(pair)) {
                double similarity = finalMapping.getSimilarity(pair);
                if (similarity >= this.minSimilarity) {
                    // Offset by 1 to ensure that the final mapping is always preferred over the initial mapping.
                    // This works because the similarity is always in the range [0, 1].
                    assignments.add(new Assignment<>(pair, similarity + 1));
                }
            }

            double similarity = initialMapping.getSimilarity(pair);
            if (similarity >= this.initialSimilarityThreshold) {
                assignments.add(new Assignment<>(pair, similarity));
            }
        }

        assignments.sort((left, right) -> Double.compare(right.similarity(), left.similarity()));
        return assignments;
    }

    private record Assignment<A, B>(Pair<A, B> pair, double similarity) {
    }
}
