package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

public interface IInstanceLink {

    IInstanceLink createCopy();

    /**
     * Returns the probability of the correctness of this link.
     *
     * @return the probability of this link
     */
    double getProbability();

    /**
     * Sets the probability to the given probability.
     *
     * @param probability the new probability
     */
    void setProbability(double probability);

    /**
     * Returns the recommended instance.
     *
     * @return the textual instance
     */
    IRecommendedInstance getTextualInstance();

    /**
     * Returns the model instance.
     *
     * @return the extracted instance
     */
    IInstance getModelInstance();

    /**
     * Returns all occurrences of all recommended instance names as string.
     *
     * @return all names of the recommended instances
     */
    String getNameOccurencesAsString();

}
