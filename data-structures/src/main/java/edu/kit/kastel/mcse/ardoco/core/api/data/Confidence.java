package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;

public class Confidence {

    private ConfidenceAggregator confidenceAggregator;

    private Map<IAgent<?>, Double> agentConfidence;

    public Confidence(ConfidenceAggregator confidenceAggregator) {
        this.confidenceAggregator = confidenceAggregator;
        this.agentConfidence = new HashMap<>();
    }

    public void addAgentConfidence(IAgent<?> agent, double confidence) {

        if (agentConfidence.containsKey(agent)) {
            throw new IllegalArgumentException("The agent has already set the confidence of this data: " + agent);
        }

        agentConfidence.put(agent, confidence);
    }

    public double getConfidence() {
        if (agentConfidence.isEmpty()) {
            return 0;
        }
        return confidenceAggregator.applyAsDouble(agentConfidence.values());
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

}
