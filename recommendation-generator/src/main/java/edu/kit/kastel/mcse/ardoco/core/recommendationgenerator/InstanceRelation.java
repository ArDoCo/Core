/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IInstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;

/**
 * Relation between RecommendedInstances, store specific occurrences as
 *
 * @see LocalRelation
 */
// NOTE: Currently, only simple relations are covered. Future versions may fromInstance and toInstance to List to
// comprise more complex relations?!
public class InstanceRelation implements IInstanceRelation {
    private Confidence probability;
    private final IRecommendedInstance fromInstance;
    private final IRecommendedInstance toInstance;
    private final List<LocalRelation> localRelations;

    @Override
    public IInstanceRelation createCopy() {
        InstanceRelation relation = new InstanceRelation(fromInstance, toInstance);
        relation.localRelations.addAll(this.localRelations);
        relation.probability = probability.createCopy();
        return relation;
    }

    private InstanceRelation(IRecommendedInstance fromInstance, IRecommendedInstance toInstance) {
        this.fromInstance = fromInstance;
        this.toInstance = toInstance;
        localRelations = new ArrayList<>();
        probability = new Confidence(AggregationFunctions.SUM);
    }

    public InstanceRelation(IRecommendedInstance fromInstance, IRecommendedInstance toInstance, IWord relator, List<IWord> from, List<IWord> to,
            IClaimant claimant) {
        this(fromInstance, toInstance);
        addLink(relator, from, to, claimant);
    }

    @Override
    public boolean addLink(IWord relator, List<IWord> from, List<IWord> to, IClaimant claimant) {
        if (relator == null || from == null || to == null) {
            return false;
        }
        for (LocalRelation relation : localRelations) {
            if (relation.from.size() == from.size() && relation.from.containsAll(from) && relation.to.size() == to.size() && relation.to.containsAll(to)) {
                return false;
            }
        }
        localRelations.add(new LocalRelation(relator, from, to));
        probability.addAgentConfidence(claimant, Math.pow(0.5, localRelations.size()));
        return true;
    }

    @Override
    public boolean matches(IRecommendedInstance fromInstance, IRecommendedInstance toInstance) {
        return this.fromInstance.equals(fromInstance) && this.toInstance.equals(toInstance);
    }

    @Override
    public boolean isIn(IWord relator, List<IWord> from, List<IWord> to) {
        for (LocalRelation relation : localRelations) {
            var sizesAreEqual = relation.from.size() == from.size() && relation.to.size() == to.size();
            var containsEqual = relation.from.containsAll(from) && relation.to.containsAll(to);
            if (relation.relator.equals(relator) && containsEqual && sizesAreEqual) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getProbability() {
        return probability.getConfidence();
    }

    @Override
    public int getSize() {
        return localRelations.size();
    }

    @Override
    public void setProbability(IClaimant claimant, double newProbability) {
        // TODO: SET != Add
        probability.addAgentConfidence(claimant, newProbability);
    }

    @Override
    public IRecommendedInstance getFromInstance() {
        return fromInstance;
    }

    @Override
    public IRecommendedInstance getToInstance() {
        return toInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromInstance, toInstance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InstanceRelation other = (InstanceRelation) obj;
        return Objects.equals(fromInstance, other.fromInstance) && Objects.equals(toInstance, other.toInstance);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("InstanceRelation{")
                .append("probability=")
                .append(probability)
                .append(", fromInstance=")
                .append(fromInstance.toString())
                .append(", ")
                .append("toInstance=")
                .append(toInstance.toString())
                .append(", ")
                .append("localRelations=");
        for (LocalRelation relation : localRelations) {
            str.append(relation.toString()).append(", ");
        }
        str.delete(str.length() - 3, str.length() - 1);
        str.append('}');
        return str.toString();
    }

    private static class LocalRelation {
        final IWord relator;
        final List<IWord> from;
        final List<IWord> to;

        LocalRelation(IWord relator, List<IWord> from, List<IWord> to) {
            this.relator = relator;
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder("Link{from=");
            for (IWord fromWord : from) {
                str.append(fromWord.getText()).append(", ");
            }
            str.deleteCharAt(str.length() - 1);
            str.append("->to=");
            for (IWord toWord : to) {
                str.append(toWord.getText()).append(", ");
            }
            str.deleteCharAt(str.length() - 1);
            str.append('}');
            return str.toString();
        }
    }
}
