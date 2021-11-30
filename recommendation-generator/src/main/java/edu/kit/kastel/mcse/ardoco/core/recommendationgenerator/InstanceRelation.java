/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;


import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Relation between RecommendedInstances, store specific occurrences as
 *
 * @see LocalRelation
 *
 *      TODO fromInstance and toInstance to List to comprise more complex relations?!
 */
public class InstanceRelation implements IInstanceRelation {
    private double probability;
    private final IRecommendedInstance fromInstance;
    private final IRecommendedInstance toInstance;
    private final List<LocalRelation> localRelations;

    @Override
    public IInstanceRelation createCopy() {
        InstanceRelation relation = new InstanceRelation(fromInstance, toInstance, null, null, null);
        for (LocalRelation localRelation : localRelations) {
            addLink(localRelation.relator, localRelation.from, localRelation.to);
        }
        return relation;
    }

    public InstanceRelation(IRecommendedInstance fromInstance, IRecommendedInstance toInstance, IWord relator, List<IWord> from, List<IWord> to) {
        this.fromInstance = fromInstance;
        this.toInstance = toInstance;
        localRelations = new ArrayList<>();
        probability = 0;
        addLink(relator, from, to);
    }

    @Override
    public boolean addLink(IWord relator, List<IWord> from, List<IWord> to) {
        if (relator == null || from == null || to == null) {
            return false;
        }
        for (LocalRelation relation : localRelations) {
            if (relation.from.size() == from.size() && relation.from.containsAll(from) && relation.to.size() == to.size() && relation.to.containsAll(to)) {
                return false;
            }
        }
        localRelations.add(new LocalRelation(relator, from, to));
        increaseProbability();
        return true;
    }

    private void increaseProbability() {
        probability += Math.pow(0.5, localRelations.size());
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
        return probability;
    }

    @Override
    public int getSize() {
        return this.localRelations.size();
    }

    @Override
    public void setProbability(double newProbability) {
        probability = newProbability;
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
        StringBuilder str = new StringBuilder("");
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

    private class LocalRelation {
        IWord relator;
        List<IWord> from;
        List<IWord> to;

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
