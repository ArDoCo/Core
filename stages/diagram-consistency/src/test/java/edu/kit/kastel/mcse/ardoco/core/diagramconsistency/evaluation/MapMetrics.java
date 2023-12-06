/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation;

import java.util.Objects;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;

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
public record MapMetrics<R, M>(MutableBiMap<R, M> truePositives, MutableBiMap<R, M> falsePositives, MutableBiMap<R, M> falseNegatives) implements Metrics {
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
    public static <R, M> MapMetrics<R, M> from(MutableBiMap<R, M> expected, MutableBiMap<R, M> actual) {
        MutableBiMap<R, M> truePositives = new HashBiMap<>();
        MutableBiMap<R, M> falsePositives = new HashBiMap<>();
        MutableBiMap<R, M> falseNegatives = new HashBiMap<>();

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
