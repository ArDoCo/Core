package edu.kit.kastel.mcse.ardoco.core.datastructures;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

import java.util.ArrayList;
import java.util.List;

public class InstanceRelation implements IInstanceRelation {
    private double probability;
    private final String lemma;
    private final List<IRecommendedInstance> fromInstances;
    private final List<IRecommendedInstance> toInstances;
    private List<LocalRelation> localRelations;

    @Override
    public IInstanceRelation createCopy() {
        InstanceRelation relation = new InstanceRelation(this.lemma, this.probability, this.fromInstances, this.toInstances, null, null, null);
        for (LocalRelation localRelation : localRelations) {
            this.addLink(localRelation.relator, localRelation.from, localRelation.to);
        }
        return relation;
    }

    public InstanceRelation(String lemma,
                            double probability,
                            List<IRecommendedInstance> fromInstances,
                            List<IRecommendedInstance> toInstances,
                            IWord relator,
                            List<IWord> from,
                            List<IWord> to) {
        this.probability = probability;
        this.fromInstances = fromInstances;
        this.toInstances = toInstances;
        this.lemma = lemma;
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
        this.probability += 1;
        return true;
    }

    @Override
    public boolean fitsRelation(String lemma, List<IRecommendedInstance> fromInstances, List<IRecommendedInstance> toInstances) {
        return this.fromInstances.size() == fromInstances.size() &&
                this.fromInstances.containsAll(fromInstances) &&
                this.toInstances.size() == toInstances.size() &&
                this.toInstances.containsAll(toInstances) &&
                this.lemma.equals(lemma);
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
    public List<IRecommendedInstance> getFromInstances() {
        return fromInstances;
    }

    @Override
    public List<IRecommendedInstance> getToInstances() {
        return toInstances;
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

        return this.fromInstances.size() == other.fromInstances.size() &&
                this.fromInstances.containsAll(other.fromInstances) &&
                this.toInstances.size() == other.toInstances.size() &&
                this.toInstances.containsAll(other.toInstances) &&
                this.lemma.equals(other.lemma);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("InstanceRelation{");
        str.append("probability=")
            .append(probability)
            .append(", lemma='")
            .append(lemma)
            .append('\'')
            .append(", fromInstances=");
        for (IRecommendedInstance instance : this.fromInstances) {
            str.append(instance.toString()).append(", ");
        }
        str.append("toInstances=");
        for (IRecommendedInstance instance : this.toInstances) {
            str.append(instance.toString()).append(", ");
        }
        str.append("localRelations=");
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
