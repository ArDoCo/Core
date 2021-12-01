/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import edu.kit.kastel.mcse.ardoco.core.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;

/**
 * The Interface IInstanceLink defines a link between an {@link IRecommendedInstance} and an {@link IModelInstance}.
 */
public interface IInstanceLink extends ICopyable<IInstanceLink> {

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
    IModelInstance getModelInstance();

    /**
     * Returns all occurrences of all recommended instance names as string.
     *
     * @return all names of the recommended instances
     */
    String getNameOccurencesAsString();

}
