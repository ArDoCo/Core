/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.common.ICopyable;

public final class Confidence implements Comparable<Confidence>, ICopyable<Confidence> {

    private final ConfidenceAggregator confidenceAggregator;

    private List<Pair<IClaimant, Double>> agentConfidence;

    public Confidence(ConfidenceAggregator confidenceAggregator) {
        this.confidenceAggregator = confidenceAggregator;
        this.agentConfidence = new ArrayList<>();
    }

    public Confidence(IClaimant claimant, double probability, ConfidenceAggregator confidenceAggregator) {
        this(confidenceAggregator);
        this.addAgentConfidence(claimant, probability);
    }

    private Confidence(ConfidenceAggregator confidenceAggregator, List<Pair<IClaimant, Double>> agentConfidence) {
        this(confidenceAggregator);
        this.agentConfidence = agentConfidence;
    }

    @Override
    public Confidence createCopy() {
        return new Confidence(this.confidenceAggregator, new ArrayList<>(this.agentConfidence));
    }

    public void addAgentConfidence(IClaimant claimant, double confidence) {
        // TODO Decide how to handle same claimants.
        /*
         * if (agentConfidence.stream().anyMatch(p -> p.getOne().equals(claimant))) { throw new
         * IllegalArgumentException("The agent has already set the confidence of this data: " + claimant); }
         */

        agentConfidence.add(Tuples.pair(claimant, confidence));
    }

    @Override
    public int compareTo(Confidence o) {
        return Double.compare(this.getConfidence(), o.getConfidence());
    }

    @Override
    public String toString() {
        return "Confidence{" + confidenceAggregator + "=>" + getConfidence() + '}';
    }

    public double getConfidence() {
        if (agentConfidence.isEmpty()) {
            return 0;
        }
        var groupAggregator = ConfidenceAggregator.MAX;
        var claimantGroupings = agentConfidence.stream().collect(Collectors.groupingBy(Pair::getOne)).values();
        var claimantConfidences = claimantGroupings.stream().map(l -> l.stream().map(Pair::getTwo).toList()).map(groupAggregator::applyAsDouble).toList();
        return confidenceAggregator.applyAsDouble(claimantConfidences);
    }

    /**
     * Merges two confidences two one w.r.t. the aggregators
     *
     * @param a                first confidence
     * @param b                second confidence
     * @param globalAggregator aggregator for merging different claimant confidences
     * @param localAggregator  aggregator for merging confidences of the same claimant
     * @return the combined confidence
     */
    public static Confidence merge(Confidence a, Confidence b, ConfidenceAggregator globalAggregator, ConfidenceAggregator localAggregator) {
        var result = new Confidence(globalAggregator);

        for (var aConf : a.agentConfidence) {
            var bConf = b.agentConfidence.stream().filter(p -> p.getOne().equals(aConf.getOne())).findFirst().orElse(null);
            if (bConf == null) {
                result.addAgentConfidence(aConf.getOne(), aConf.getTwo());
            } else {
                result.addAgentConfidence(aConf.getOne(), localAggregator.applyAsDouble(List.of(aConf.getTwo(), bConf.getTwo())));
            }
        }

        for (var bConf : b.agentConfidence) {
            var aConf = a.agentConfidence.stream().anyMatch(p -> p.getOne().equals(bConf.getOne()));
            if (!aConf) {
                result.addAgentConfidence(bConf.getOne(), bConf.getTwo());
            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentConfidence, confidenceAggregator);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (Confidence) obj;
        return Objects.equals(agentConfidence, other.agentConfidence) && confidenceAggregator == other.confidenceAggregator;
    }

    public enum ConfidenceAggregator implements ToDoubleFunction<Collection<Double>> {
        /**
         * Use the median of the scores as final score.
         */
        MEDIAN(s -> {
            var sortedNormalized = s.stream().sorted().toList();
            var sizeHalf = sortedNormalized.size() / 2;

            if (sortedNormalized.size() % 2 == 0) {
                return (sortedNormalized.get(sizeHalf) + sortedNormalized.get(sizeHalf - 1)) / 2;
            }
            return sortedNormalized.get(sizeHalf);
        }),

        HARMONIC(s -> {
            var quotient = s.stream().mapToDouble(d -> 1.0 / d).sum();
            return s.size() / quotient;
        }),

        ROOTMEANSQUARED(s -> {
            var squaredValuesSum = s.stream().mapToDouble(d -> Math.pow(d, 2)).sum();
            return Math.sqrt(squaredValuesSum / s.size());
        }),

        CUBICMEAN(s -> {
            var squaredValuesSum = s.stream().mapToDouble(d -> Math.pow(d, 3)).sum();
            return Math.cbrt(squaredValuesSum / s.size());
        }),

        USE_MOST_RECENT(s -> s.stream().reduce((first, second) -> second).orElse(0.0)),

        /**
         * Use the average of the scores as final score.
         */
        AVERAGE(s -> s.stream().mapToDouble(d -> d).average().getAsDouble()),

        /**
         * Use the max value of the scores as final score.
         */
        MAX(s -> s.stream().mapToDouble(d -> d).max().getAsDouble()),

        /**
         * Use the min value of the scores as final score.
         */
        MIN(s -> s.stream().mapToDouble(d -> d).min().getAsDouble()),

        /**
         * Use the sum of the scores as final score.
         */
        SUM(s -> s.stream().mapToDouble(d -> d).sum());

        private final ToDoubleFunction<Collection<Double>> function;

        ConfidenceAggregator(ToDoubleFunction<Collection<Double>> function) {
            this.function = function;
        }

        @Override
        public double applyAsDouble(Collection<Double> value) {
            return this.function.applyAsDouble(value);
        }
    }

}
