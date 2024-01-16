/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.collections.api.bimap.MutableBiMap;

/**
 * Metrics of the diagram-model matching process.
 *
 * @param truePositives
 *                       All true positive links.
 * @param falsePositives
 *                       All false positive links.
 * @param falseNegatives
 *                       All false negative links.
 * @param <R>
 *                       The type of the diagram elements.
 * @param <M>
 *                       The type of the model elements.
 */
public record MapMetrics<R, M>(Map<R, M> truePositives, Map<R, M> falsePositives, Map<R, M> falseNegatives) implements Metrics {
    /**
     * Create a new metrics object from the expected and actual links.
     *
     * @param expected
     *                 The expected links.
     * @param actual
     *                 The actual links.
     * @param <R>
     *                 The type of the diagram elements.
     * @param <M>
     *                 The type of the model elements.
     * @return The metrics.
     */
    public static <R, M> MapMetrics<R, M> from(Map<R, M> expected, MutableBiMap<R, M> actual) {
        Map<R, M> truePositives = new IdentityHashMap<>();
        Map<R, M> falsePositives = new IdentityHashMap<>();
        Map<R, M> falseNegatives = new IdentityHashMap<>();

        for (var entry : actual.entrySet()) {
            if (Objects.equals(expected.get(entry.getKey()), entry.getValue())) {
                truePositives.put(entry.getKey(), entry.getValue());
            } else {
                falsePositives.put(entry.getKey(), entry.getValue());
            }
        }

        for (var entry : expected.entrySet()) {
            if (!Objects.equals(actual.get(entry.getKey()), entry.getValue())) {
                falseNegatives.put(entry.getKey(), entry.getValue());
            }
        }

        return new MapMetrics<>(truePositives, falsePositives, falseNegatives);
    }

    @Override
    public int getTruePositiveCount() {
        return this.truePositives.size();
    }

    @Override
    public int getFalsePositiveCount() {
        return this.falsePositives.size();
    }

    @Override
    public int getFalseNegativeCount() {
        return this.falseNegatives.size();
    }
}
