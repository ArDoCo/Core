/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Triple;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public final class Confidence implements Comparable<Confidence>, ICopyable<Confidence> {

    private final AggregationFunctions confidenceAggregator;

    // Claimant, Confidence, MethodName
    private List<Triple<Claimant, Double, String>> agentConfidences;

    public Confidence(AggregationFunctions confidenceAggregator) {
        this.confidenceAggregator = confidenceAggregator;
        this.agentConfidences = new ArrayList<>();
    }

    public Confidence(Claimant claimant, double probability, AggregationFunctions confidenceAggregator) {
        this(confidenceAggregator);
        this.addAgentConfidence(claimant, probability);
    }

    private Confidence(AggregationFunctions confidenceAggregator, List<Triple<Claimant, Double, String>> agentConfidence) {
        this(confidenceAggregator);
        this.agentConfidences = new ArrayList<>(agentConfidence);
    }

    public Set<Claimant> getClaimants() {
        return this.agentConfidences.stream().map(Triple::first).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Confidence createCopy() {
        return new Confidence(this.confidenceAggregator, this.agentConfidences);
    }

    public void addAgentConfidence(Claimant claimant, double confidence) {
        String method = getMethodInClaimant(claimant);
        agentConfidences.add(new Triple<>(claimant, confidence, method));
    }

    private String getMethodInClaimant(Claimant claimant) {
        var trace = new Exception().getStackTrace();
        for (var te : trace) {
            if (te.getClassName().equals(claimant.getClass().getName())) {
                return te.getMethodName();
            }
        }
        return "Unknown Method";
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
        if (agentConfidences.isEmpty()) {
            return 0;
        }
        if (confidenceAggregator == AggregationFunctions.ROLLING_AVERAGE) {
            // No aggregate
            return confidenceAggregator.applyAsDouble(agentConfidences.stream().map(Triple::second).toList());
        }
        var groupAggregator = AggregationFunctions.MAX;
        var claimantGroupings = agentConfidences.stream().collect(Collectors.groupingBy(Triple::first)).values();
        var claimantConfidences = claimantGroupings.stream().map(l -> l.stream().map(Triple::second).toList()).map(groupAggregator::applyAsDouble).toList();
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
    public static Confidence merge(Confidence a, Confidence b, AggregationFunctions globalAggregator, AggregationFunctions localAggregator) {
        var result = new Confidence(globalAggregator);

        for (var aConf : a.agentConfidences) {
            var bConf = b.agentConfidences.stream().filter(p -> p.first().equals(aConf.first())).findFirst().orElse(null);
            if (bConf == null) {
                result.addAgentConfidence(aConf.first(), aConf.second());
            } else {
                result.addAgentConfidence(aConf.first(), localAggregator.applyAsDouble(List.of(aConf.second(), bConf.second())));
            }
        }

        for (var bConf : b.agentConfidences) {
            var aConf = a.agentConfidences.stream().anyMatch(p -> p.first().equals(bConf.first()));
            if (!aConf) {
                result.addAgentConfidence(bConf.first(), bConf.second());
            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentConfidences, confidenceAggregator);
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
        return Objects.equals(agentConfidences, other.agentConfidences) && confidenceAggregator == other.confidenceAggregator;
    }

    public void addAllConfidences(Confidence other) {
        this.agentConfidences.addAll(other.agentConfidences);
    }
}
