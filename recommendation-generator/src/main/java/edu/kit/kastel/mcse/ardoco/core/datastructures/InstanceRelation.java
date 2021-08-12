package edu.kit.kastel.mcse.ardoco.core.datastructures;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

import java.util.ArrayList;
import java.util.List;

/**
 * Relation between RecommendedInstances, store specific occurrences as
 * @see LocalRelation
 *
 * TODO fromInstance and toInstance to List to comprise more complex relations?!
 */
public class InstanceRelation implements IInstanceRelation {
    private double probability;
    private final IRecommendedInstance fromInstance;
    private final IRecommendedInstance toInstance;
    private final List<LocalRelation> localRelations;

    @Override
    public IInstanceRelation createCopy() {
        InstanceRelation relation = new InstanceRelation(this.fromInstance, this.toInstance, null, null, null);
        for (LocalRelation localRelation : localRelations) {
            this.addLink(localRelation.relator, localRelation.from, localRelation.to);
        }
        return relation;
    }

    public InstanceRelation(IRecommendedInstance fromInstance,
                            IRecommendedInstance toInstance,
                            IWord relator,
                            List<IWord> from,
                            List<IWord> to) {
        this.fromInstance = fromInstance;
        this.toInstance = toInstance;
        this.localRelations = new ArrayList<>();
        this.addLink(relator, from, to);
    }

    @Override
    public boolean addLink(IWord relator, List<IWord> from, List<IWord> to) {
        if (relator == null || from == null || to == null) {
            return false;
        }
        for (LocalRelation relation : this.localRelations) {
            if (relation.from.size() == from.size() &&
                    relation.from.containsAll(from) &&
                    relation.to.size() == to.size() &&
                    relation.to.containsAll(to)) {
                return false;
            }
        }
        this.localRelations.add(new LocalRelation(relator, from, to));
        this.probability += (this.getFromInstance().getProbability() + this.getToInstance().getProbability()) / 2;
        return true;
    }

    @Override
    public boolean matches(IRecommendedInstance fromInstance, IRecommendedInstance toInstance) {
        return this.fromInstance.equals(fromInstance) && this.toInstance.equals(toInstance);
    }

    @Override
    public boolean isIn(IWord relator, List<IWord> from, List<IWord> to) {
        for (LocalRelation relation : this.localRelations) {
            if (relation.relator.equals(relator) &&
                relation.from.size() == from.size() &&
                relation.from.containsAll(from) &&
                relation.to.size() == to.size() &&
                relation.to.containsAll(to)) {
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
    public void setProbability(double newProbability) {
        this.probability = newProbability;
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InstanceRelation other = (InstanceRelation) obj;

        return this.fromInstance == other.fromInstance &&
                this.toInstance == other.toInstance;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("");
        str.append("InstanceRelation{")
            .append("probability=")
            .append(probability)
            .append(", fromInstance=")
            .append(this.fromInstance.toString())
            .append(", ")
            .append("toInstance=")
            .append(this.toInstance.toString())
            .append(", ")
            .append("localRelations=");
        for (LocalRelation relation : this.localRelations) {
            str.append(relation.toString())
                    .append(", ");
        }
        str.delete(str.length() - 3, str.length() - 1);
        str.append('}');
        return str.toString();
    }

    private class LocalRelation {
        IWord relator;
        List<IWord> from;
        List<IWord> to;
        int sentenceNo;
        LocalRelation(IWord relator, List<IWord> from, List<IWord> to) {
            this.relator = relator;
            this.from = from;
            this.to = to;
            this.sentenceNo = relator.getSentenceNo();
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder("Link{from=");
            for (IWord fromWord : from) {
                str.append(fromWord.getText()).append(", ");
            }
            str.deleteCharAt(str.length()-1);
            str.append("->to=");
            for (IWord toWord : to) {
                str.append(toWord.getText()).append(", ");
            }
            str.deleteCharAt(str.length()-1);
            str.append('}');
            return str.toString();
        }
    }
}
