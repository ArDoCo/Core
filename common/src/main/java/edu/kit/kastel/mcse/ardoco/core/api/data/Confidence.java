/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.tuple.Triple;
import org.eclipse.collections.impl.tuple.Tuples;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;

public final class Confidence implements Comparable<Confidence>, ICopyable<Confidence> {

    private final AggregationFunctions confidenceAggregator;

    // Claimant, Confidence, MethodName
    private MutableList<Triple<Claimant, Double, String>> agentConfidences;

    public Confidence(AggregationFunctions confidenceAggregator) {
        this.confidenceAggregator = confidenceAggregator;
        this.agentConfidences = Lists.mutable.empty();
    }

    public Confidence(Claimant claimant, double probability, AggregationFunctions confidenceAggregator) {
        this(confidenceAggregator);
        this.addAgentConfidence(claimant, probability);
    }

    private Confidence(AggregationFunctions confidenceAggregator, ImmutableList<Triple<Claimant, Double, String>> agentConfidence) {
        this(confidenceAggregator);
        this.agentConfidences = Lists.mutable.withAll(agentConfidence);
    }

    public ImmutableSet<Claimant> getClaimants() {
        return this.agentConfidences.collect(Triple::getOne).toImmutableSet();
    }

    @Override
    public Confidence createCopy() {
        return new Confidence(this.confidenceAggregator, this.agentConfidences.toImmutable());
    }

    public void addAgentConfidence(Claimant claimant, double confidence) {
        String method = getMethodInClaimant(claimant);
        agentConfidences.add(Tuples.triple(claimant, confidence, method));
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
            return confidenceAggregator.applyAsDouble(agentConfidences.stream().map(Triple::getTwo).toList());
        }
        var groupAggregator = AggregationFunctions.MAX;
        var claimantGroupings = agentConfidences.stream().collect(Collectors.groupingBy(Triple::getOne)).values();
        var claimantConfidences = claimantGroupings.stream().map(l -> l.stream().map(Triple::getTwo).toList()).map(groupAggregator::applyAsDouble).toList();
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

    public void addAllConfidences(Confidence other) {
        this.agentConfidences.addAll(other.agentConfidences);
    }
}
