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

/**
 * This class represents a confidence for a certain (intermediate) result.
 * Different {@link Claimant Claimants} can add their confidences that get aggregated via one of the {@link AggregationFunctions} to a single confidence value.
 */
public final class Confidence implements Comparable<Confidence>, ICopyable<Confidence> {

    private final AggregationFunctions confidenceAggregator;

    // Claimant, Confidence, MethodName
    private List<Triple<Claimant, Double, String>> agentConfidences;

    /**
     * Constructor for the confidence with a given aggregator function.
     *
     * @param confidenceAggregator the aggregation function for the confidence
     */
    public Confidence(AggregationFunctions confidenceAggregator) {
        this.confidenceAggregator = confidenceAggregator;
        this.agentConfidences = new ArrayList<>();
    }

    /**
     * Constructor for the confidence with a given aggregator function and an initial claimant with a certain probability (confidence).
     *
     * @param claimant             the claimant
     * @param probability          the probability
     * @param confidenceAggregator the aggregation function
     */
    public Confidence(Claimant claimant, double probability, AggregationFunctions confidenceAggregator) {
        this(confidenceAggregator);
        this.addAgentConfidence(claimant, probability);
    }

    private Confidence(AggregationFunctions confidenceAggregator, List<Triple<Claimant, Double, String>> agentConfidence) {
        this(confidenceAggregator);
        this.agentConfidences = new ArrayList<>(agentConfidence);
    }

    /**
     * Returns the set of claimants that contribute to this confidence
     *
     * @return the claimants
     */
    public Set<Claimant> getClaimants() {
        return this.agentConfidences.stream().map(Triple::first).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Confidence createCopy() {
        return new Confidence(this.confidenceAggregator, this.agentConfidences);
    }

    /**
     * Add a confidence of an agent ({@link Claimant}.
     *
     * @param claimant   the claimant
     * @param confidence the confidence
     */
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

    /**
     * Returns the (aggregated) confidence value
     *
     * @return the (aggregated) confidence value
     */
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
