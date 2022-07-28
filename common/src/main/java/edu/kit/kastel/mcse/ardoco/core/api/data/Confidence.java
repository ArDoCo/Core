/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Sophie Schulz
 * @author Dominik Fuchß
 * @author Jan Keim
 */
public final class Confidence implements Comparable<Confidence>, ICopyable<Confidence> {

    private final AggregationFunctions confidenceAggregator;

    private List<Pair<Claimant, Double>> agentConfidences;

    public Confidence(AggregationFunctions confidenceAggregator) {
        this.confidenceAggregator = confidenceAggregator;
        this.agentConfidences = new ArrayList<>();
    }

    public Confidence(Claimant claimant, double probability, AggregationFunctions confidenceAggregator) {
        this(confidenceAggregator);
        this.addAgentConfidence(claimant, probability);
    }

    private Confidence(AggregationFunctions confidenceAggregator, List<Pair<Claimant, Double>> agentConfidence) {
        this(confidenceAggregator);
        this.agentConfidences = agentConfidence;
    }

    @Override
    public Confidence createCopy() {
        return new Confidence(this.confidenceAggregator, new ArrayList<>(this.agentConfidences));
    }

    public Confidence forClaimant(Claimant claimant) {
        return new Confidence(this.confidenceAggregator, new ArrayList<>(this.agentConfidences.stream().filter(it -> it.getOne().equals(claimant)).toList()));
    }

    public void addAgentConfidence(Claimant claimant, double confidence) {
        agentConfidences.add(Tuples.pair(claimant, confidence));
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
            return confidenceAggregator.applyAsDouble(agentConfidences.stream().map(Pair::getTwo).toList());
        }
        var groupAggregator = AggregationFunctions.MAX;
        var claimantGroupings = agentConfidences.stream().collect(Collectors.groupingBy(Pair::getOne)).values();
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
    public static Confidence merge(Confidence a, Confidence b, AggregationFunctions globalAggregator, AggregationFunctions localAggregator) {
        var result = new Confidence(globalAggregator);

        for (var aConf : a.agentConfidences) {
            var bConf = b.agentConfidences.stream().filter(p -> p.getOne().equals(aConf.getOne())).findFirst().orElse(null);
            if (bConf == null) {
                result.addAgentConfidence(aConf.getOne(), aConf.getTwo());
            } else {
                result.addAgentConfidence(aConf.getOne(), localAggregator.applyAsDouble(List.of(aConf.getTwo(), bConf.getTwo())));
            }
        }

        for (var bConf : b.agentConfidences) {
            var aConf = a.agentConfidences.stream().anyMatch(p -> p.getOne().equals(bConf.getOne()));
            if (!aConf) {
                result.addAgentConfidence(bConf.getOne(), bConf.getTwo());
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

}
