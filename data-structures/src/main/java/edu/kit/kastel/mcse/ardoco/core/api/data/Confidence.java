package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;

public final class Confidence implements Comparable<Confidence> {

    private ConfidenceAggregator confidenceAggregator;

    private List<Pair<IAgent<?>, Double>> agentConfidence;

    public Confidence(ConfidenceAggregator confidenceAggregator) {
        this.confidenceAggregator = confidenceAggregator;
        this.agentConfidence = new ArrayList<>();
    }

    public Confidence(IAgent<?> claimant, double probability, ConfidenceAggregator confidenceAggregator) {
        this(confidenceAggregator);
        this.addAgentConfidence(claimant, probability);
    }

    public void addAgentConfidence(IAgent<?> claimant, double confidence) {

        if (agentConfidence.stream().anyMatch(p -> p.getOne().equals(claimant))) {
            throw new IllegalArgumentException("The agent has already set the confidence of this data: " + claimant);
        }

        agentConfidence.add(Tuples.pair(claimant, confidence));
    }

    public double getConfidence() {
        if (agentConfidence.isEmpty()) {
            return 0;
        }
        return confidenceAggregator.applyAsDouble(agentConfidence.stream().map(p -> p.getTwo()).toList());
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
        Confidence result = new Confidence(globalAggregator);

        for (var aConf : a.agentConfidence) {
            Pair<IAgent<?>, Double> bConf = b.agentConfidence.stream().filter(p -> p.getOne().equals(aConf.getOne())).findFirst().orElse(null);
            if (bConf == null) {
                result.addAgentConfidence(aConf.getOne(), aConf.getTwo());
            } else {
                result.addAgentConfidence(aConf.getOne(), localAggregator.applyAsDouble(List.of(aConf.getTwo(), bConf.getTwo())));
            }
        }

        for (var bConf : b.agentConfidence) {
            boolean aConf = a.agentConfidence.stream().anyMatch(p -> p.getOne().equals(bConf.getOne()));
            if (!aConf) {
                result.addAgentConfidence(bConf.getOne(), bConf.getTwo());
            }
        }

        return result;
    }

    public enum ConfidenceAggregator implements ToDoubleFunction<Collection<Double>> {
        /**
         * Use the median of the scores as final score.
         */
        MEDIAN(s -> {
            var sortedNormalized = s.stream().sorted().collect(Collectors.toList());
            int sizeHalf = sortedNormalized.size() / 2;

            if (sortedNormalized.size() % 2 == 0) {
                return (sortedNormalized.get(sizeHalf) + sortedNormalized.get(sizeHalf - 1)) / 2;
            } else {
                return sortedNormalized.get(sizeHalf);
            }
        }),

        HARMONIC(s -> {
            var quotient = s.stream().mapToDouble(d -> 1.0 / d).sum();
            return s.size() / quotient;
        }),

        USE_MOST_RECENT(s -> {
            return s.stream().reduce((first, second) -> second).orElse(0.0);

        }),

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
        MIN(s -> s.stream().mapToDouble(d -> d).min().getAsDouble());

        private ToDoubleFunction<Collection<Double>> function;

        ConfidenceAggregator(ToDoubleFunction<Collection<Double>> function) {
            this.function = function;
        }

        @Override
        public double applyAsDouble(Collection<Double> value) {
            return this.function.applyAsDouble(value);
        }
    }

    @Override
    public int compareTo(Confidence o) {
        return Double.compare(this.getConfidence(), o.getConfidence());
    }

}
