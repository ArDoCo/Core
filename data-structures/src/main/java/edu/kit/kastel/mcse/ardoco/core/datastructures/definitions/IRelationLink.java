package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

public interface IRelationLink {

    IRelationLink createCopy();

    /**
     * Returns the probability of the correctness of this link.
     *
     * @return probability of the mapping.
     */
    double getProbability();

    /**
     * Sets the probability of this mapping to the given probability.
     *
     * @param probability the new probability
     */
    void setProbability(double probability);

    /**
     * Returns the recommended relation of this link.
     *
     * @return the textual relation
     */
    IRecommendedRelation getTextualRelation();

    /**
     * Returns the relation of the model extraction state of this link.
     *
     * @return the relation of the model
     */
    IRelation getModelRelation();

}
