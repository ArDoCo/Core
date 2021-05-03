package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

public interface INounMapping {

    INounMapping createCopy();

    /**
     * Splits all occurrences with a whitespace in it at their spaces and returns all parts that are similar to the
     * reference. If it contains a separator or similar to the reference it is added to the comparables as a whole.
     *
     * @return all parts of occurrences (splitted at their spaces) that are similar to the reference.
     */
    List<String> getRepresentativeComparables();

    /**
     * Sets the probability of the mapping
     *
     * @param probability probability to set on
     */
    void hardSetProbability(double probability);

    /**
     * Returns the occurrences of this mapping.
     *
     * @return all appearances of the mapping
     */
    List<String> getOccurrences();

    /**
     * Returns all nodes contained by the mapping
     *
     * @return all mapping nodes
     */
    List<IWord> getWords();

    /**
     * Adds nodes to the mapping, if they are not already contained.
     *
     * @param nodes graph nodes to add to the mapping
     */
    void addNodes(List<IWord> nodes);

    /**
     * Adds a node to the mapping, it its not already contained.
     *
     * @param n graph node to add.
     */
    void addNode(IWord n);

    /**
     * Returns the probability of being a mapping of its kind.
     *
     * @return probability of being a mapping of its kind.
     */
    double getProbability();

    /**
     * Returns the kind: name, type, name_or_type.
     *
     * @return the kind
     */
    MappingKind getKind();

    /**
     * Returns the reference, the comparable and naming attribute of this mapping.
     *
     * @return the reference
     */
    String getReference();

    /**
     * Changes the kind to another one and recalculates the probability with
     * {@link #recalculateProbability(double, double)}.
     *
     * @param kind        the new kind
     * @param probability the probability of the new mappingTzpe
     */
    void changeMappingType(MappingKind kind, double probability);

    /**
     * Returns the sentence numbers of occurrences, sorted.
     *
     * @return sentence numbers of the occurrences of this mapping.
     */
    List<Integer> getMappingSentenceNo();

    /**
     * Updates the reference if the probability is high enough.
     *
     * @param ref         new reference
     * @param probability probability for the new reference.
     */
    void updateReference(String ref, double probability);

    /**
     * Adds occurrences to the mapping
     *
     * @param occurrences2 occurrences to add
     */
    void addOccurrence(List<String> occurrences2);

    /**
     * Copies all nodes and occurrences matching the occurrence to another mapping
     *
     * @param occurrence     the occurrence to copy
     * @param createdMapping the other mapping
     */
    void copyOccurrencesAndNodesTo(String occurrence, INounMapping createdMapping);

    /**
     * Updates the probability
     *
     * @param newProbability the probability to update with.
     */
    void updateProbability(double newProbability);

    double getProbabilityForName();

    double getProbabilityForType();

    double getProbabilityForNort();
}
