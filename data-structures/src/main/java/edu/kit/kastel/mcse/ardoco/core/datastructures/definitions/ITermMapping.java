package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

public interface ITermMapping {

    ITermMapping createCopy();

    /**
     * Returns the mappings, of the term.
     *
     * @return a list of mappings of the term.
     */
    List<INounMapping> getMappings();

    /**
     * Returns the probability that this mapping is a term of the given kind (multi type).
     *
     * @return the probability of this mapping as double.
     */
    double getProbability();

    /**
     * Returns the reference of this term.
     *
     * @return the reference of the term.
     */
    String getReference();

    /**
     * Returns the type of this term.
     *
     * @return the type of this term.
     */
    MappingKind getKind();

    /**
     * Updates the probability
     *
     * @param probability2 the probability to update with.
     */
    void updateProbability(double probability2);

    /**
     * Sets the probability of the mapping
     *
     * @param probability probability to set on
     */
    void hardSetProbability(double probability);

}
