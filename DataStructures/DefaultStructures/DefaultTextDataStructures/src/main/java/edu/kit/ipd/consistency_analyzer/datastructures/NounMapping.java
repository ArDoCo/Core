package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;

/**
 * This class represents a noun, which is supposed to be a name or type of an instance. It is just textual related. The
 * identifier of such a mapping is the reference, containing the word all occurrences are familiar with. The nodes are
 * referencing all occurrences of the reference. Thus, the reference contains the name or type of the mapping. The
 * probability is the probability, that this mapping is of its MappingType. The MappingType declares what the mapping is
 * supposed to be: A name, type or both (if its not decided yet).
 *
 * @author Sophie
 *
 */
public final class NounMapping implements INounMapping {

    private List<IWord> nodes;
    private double probability;
    private MappingKind kind;
    private String reference;
    private List<String> occurrences;

    @Override
    public INounMapping createCopy() {

        return new NounMapping(nodes, probability, kind, reference, occurrences);

    }

    /**
     * Creates a new mapping.
     *
     * @param nodes       nodes representing the mapping.
     * @param probability of being of that mappingType.
     * @param kind        the kind of the mapping
     * @param reference   the reference, the unique name, of the mapping. This reference is used for comparison and
     *                    should be mostly general.
     * @param occurrences the occurrences, e.g. values of the nodes, that appears at the nodes listed in this mapping.
     */
    public NounMapping(List<IWord> nodes, double probability, MappingKind kind, String reference, List<String> occurrences) {

        this.nodes = new ArrayList<>(nodes);
        this.probability = probability;
        this.kind = kind;
        this.reference = reference;
        this.occurrences = new ArrayList<>(occurrences);
    }

    /**
     * Splits all occurrences with a whitespace in it at their spaces and returns all parts that are similar to the
     * reference. If it contains a separator or similar to the reference it is added to the comparables as a whole.
     *
     * @return all parts of occurrences (splitted at their spaces) that are similar to the reference.
     */
    @Override
    public List<String> getRepresentativeComparables() {
        List<String> comparables = new ArrayList<>();
        for (String occ : occurrences) {
            if (SimilarityUtils.containsSeparator(occ)) {
                List<String> parts = SimilarityUtils.splitAtSeparators(occ);
                for (String part : parts) {
                    if (SimilarityUtils.areWordsSimilar(reference, part)) {
                        comparables.add(part);
                    }
                }
                comparables.add(occ);
            } else {
                if (SimilarityUtils.areWordsSimilar(reference, occ)) {
                    comparables.add(occ);
                }
            }
        }
        return comparables;
    }

    /**
     * Sets the probability of the mapping
     *
     * @param probability probability to set on
     */
    @Override
    public void hardSetProbability(double probability) {
        this.probability = probability;
    }

    /**
     * Returns the occurrences of this mapping.
     *
     * @return all appearances of the mapping
     */
    @Override
    public List<String> getOccurrences() {
        return new ArrayList<>(occurrences);
    }

    /**
     * Returns all nodes contained by the mapping
     *
     * @return all mapping nodes
     */
    @Override
    public List<IWord> getNodes() {
        return new ArrayList<>(nodes);
    }

    /**
     * Adds nodes to the mapping, if they are not already contained.
     *
     * @param nodes graph nodes to add to the mapping
     */
    @Override
    public void addNodes(List<IWord> nodes) {
        for (IWord n : nodes) {
            addNode(n);
        }
    }

    /**
     * Adds a node to the mapping, it its not already contained.
     *
     * @param n graph node to add.
     */
    @Override
    public void addNode(IWord n) {
        if (!nodes.contains(n)) {
            nodes.add(n);
        }
    }

    /**
     * Returns the probability of being a mapping of its kind.
     *
     * @return probability of being a mapping of its kind.
     */
    @Override
    public double getProbability() {
        return probability;
    }

    /**
     * Returns the kind: name, type, name_or_type.
     *
     * @return the kind
     */
    @Override
    public MappingKind getKind() {
        return kind;
    }

    /**
     * Returns the reference, the comparable and naming attribute of this mapping.
     *
     * @return the reference
     */
    @Override
    public String getReference() {
        return reference;
    }

    /**
     * Changes the kind to another one and recalculates the probability with
     * {@link #recalculateProbability(double, double)}.
     *
     * @param kind        the new kind
     * @param probability the probability of the new mappingTzpe
     */
    @Override
    public void changeMappingType(MappingKind kind, double probability) {
        this.kind = kind;
        this.probability = recalculateProbability(this.probability, probability);
    }

    /**
     * Recalculates the probability.
     *
     * @param beforeProbability the probability of the mapping before the change
     * @param afterProbability  the probability of the mapping after the change
     * @return the resulting probability.
     */
    private double recalculateProbability(double beforeProbability, double afterProbability) {
        return beforeProbability * afterProbability;
    }

    /**
     * Returns the sentence numbers of occurrences, sorted.
     *
     * @return sentence numbers of the occurrences of this mapping.
     */
    @Override
    public List<Integer> getMappingSentenceNo() {
        List<Integer> positions = new ArrayList<>();
        for (IWord n : nodes) {
            positions.add(n.getSentenceNo() + 1);
        }
        Collections.sort(positions);
        return positions;
    }

    /**
     * Updates the reference if the probability is high enough.
     *
     * @param ref         new reference
     * @param probability probability for the new reference.
     */
    @Override
    public void updateReference(String ref, double probability) {
        if (probability > this.probability * 4) {
            reference = ref;
        }
    }

    @Override
    public String toString() {
        return "NounMapping [" + "mappingTypea=" + kind + //
                ", reference=" + reference + //
                ", node=" + String.join(", ", occurrences) + //
                ", position=" + String.join(", ", nodes.stream().map(word -> String.valueOf(word.getPosition())).collect(Collectors.toList())) + //
                ", probability=" + probability + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, nodes, occurrences);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NounMapping other = (NounMapping) obj;

        if (!SimilarityUtils.areWordsSimilar(reference, other.reference) && !reference.contentEquals("") && !other.reference.contentEquals("")) {
            return false;
        }
        return kind == other.kind && Objects.equals(nodes, other.nodes) && //
                Objects.equals(occurrences, other.occurrences) && SimilarityUtils.areWordsSimilar(reference, other.reference);
    }

    /**
     * Adds occurrences to the mapping
     *
     * @param occurrences2 occurrences to add
     */
    @Override
    public void addOccurrence(List<String> occurrences2) {
        for (String occ2 : occurrences2) {
            if (!occurrences.contains(occ2)) {
                occurrences.add(occ2);
            }
        }
    }

    /**
     * Copies all nodes and occurrences matching the occurrence to another mapping
     *
     * @param occurrence     the occurrence to copy
     * @param createdMapping the other mapping
     */
    @Override
    public void copyOccurrencesAndNodesTo(String occurrence, INounMapping createdMapping) {
        List<IWord> occNodes = nodes.stream().filter(n -> n.getText().equals(occurrence)).collect(Collectors.toList());
        createdMapping.addNodes(occNodes);
        createdMapping.addOccurrence(List.of(occurrence));

    }

    /**
     * Returns a list of all node lemmas encapsulated by a mapping.
     *
     * @return list of containing node lemmas
     */
    public List<String> getMappingLemmas() {
        return nodes.stream().map(IWord::getLemma).collect(Collectors.toList());
    }

    /**
     * Updates the probability
     *
     * @param newProbability the probability to update with.
     */
    @Override
    public void updateProbability(double newProbability) {
        if (probability == 1.0) {
            // do nothing
        } else if (newProbability == 1.0) {
            probability = 1.0;
        } else if (probability >= newProbability) {
            probability += newProbability * (1 - probability);
        } else {
            probability += newProbability;
            probability = probability * 0.5;
        }
    }

    @Override
    public double getProbabilityForName() {
        if (kind.equals(MappingKind.NAME)) {
            return probability;
        } else {
            return 0;
        }
    }

    @Override
    public double getProbabilityForType() {
        if (kind.equals(MappingKind.TYPE)) {
            return probability;
        } else {
            return 0;
        }
    }

    @Override
    public double getProbabilityForNort() {
        if (kind.equals(MappingKind.NAME_OR_TYPE)) {
            return probability;
        } else {
            return 0;
        }
    }

}
