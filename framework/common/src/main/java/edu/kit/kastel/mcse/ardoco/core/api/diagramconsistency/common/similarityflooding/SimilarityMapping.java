/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jgrapht.alg.util.Pair;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Stores the similarities of pairs of elements.
 *
 * @param <A>
 *            The vertex type of the first graph.
 * @param <B>
 *            The vertex type of the second graph.
 */
@Deterministic
public class SimilarityMapping<A, B> {
    private final Map<Pair<A, B>, Double> similarities = new LinkedHashMap<>();
    private final Function<Pair<A, B>, Double> defaultProvider;

    /**
     * Create a new instance of the SimilarityMap, where every element is mapped to the given similarity.
     *
     * @param defaultSimilarity
     *                          The similarity to use for every element.
     */
    public SimilarityMapping(double defaultSimilarity) {
        this.defaultProvider = pair -> defaultSimilarity;
    }

    /**
     * Create a new instance of the SimilarityMap, where every element is mapped to the similarity provided by the given
     * function. The function is never called more than once for the same element.
     *
     * @param defaultProvider
     *                        The function that provides the similarity for every element.
     */
    public SimilarityMapping(Function<Pair<A, B>, Double> defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    /**
     * Initialize with two lists that can be zipped together.
     *
     * @param entries
     *                     The list of entries.
     * @param similarities
     *                     The list of similarities.
     */
    public SimilarityMapping(List<Pair<A, B>> entries, List<Double> similarities) {
        this.defaultProvider = pair -> {
            throw new IllegalArgumentException("No similarity for pair " + pair);
        };

        if (entries.size() != similarities.size()) {
            throw new IllegalArgumentException("The number of entries and similarities must be equal.");
        }

        for (int i = 0; i < entries.size(); i++) {
            this.similarities.put(entries.get(i), similarities.get(i));
        }
    }

    /**
     * Get all elements that have an explicit similarity mapping.
     *
     * @return The set of elements.
     */
    public Set<Pair<A, B>> getMappedElements() {
        return this.similarities.keySet();
    }

    /**
     * Get all similarities of the given elements.
     *
     * @param entries
     *                The elements.
     * @return Their similarities.
     */
    public List<Double> getMappedValues(List<Pair<A, B>> entries) {
        return entries.stream().map(this::getSimilarity).toList();
    }

    /**
     * Update the similarity of a pair of elements.
     *
     * @param pair
     *                   The element pair.
     * @param similarity
     *                   The new similarity.
     */
    public void updateSimilarity(Pair<A, B> pair, double similarity) {
        this.similarities.put(pair, similarity);
    }

    /**
     * Get the similarity of a pair of elements.
     *
     * @param pair
     *             The element pair.
     * @return The similarity of the pair.
     */
    public double getSimilarity(Pair<A, B> pair) {
        return this.similarities.computeIfAbsent(pair, this.defaultProvider);
    }

    /**
     * Get the similarity of a pair of elements.
     *
     * @param a
     *          The first element of the pair.
     * @param b
     *          The second element of the pair.
     * @return The similarity of the pair.
     */
    public double getSimilarity(A a, B b) {
        return this.getSimilarity(new Pair<>(a, b));
    }

    /**
     * Prepare the similarity map for the cartesian product of the given collections. This means that the default
     * similarity is computed for every pair of elements.
     *
     * @param a
     *          The first collection.
     * @param b
     *          The second collection.
     */
    public void prepareCartesian(Collection<A> a, Collection<B> b) {
        for (A a1 : a) {
            for (B b1 : b) {
                this.getSimilarity(a1, b1);
            }
        }
    }

    @Override
    public String toString() {
        return this.similarities.toString();
    }
}
