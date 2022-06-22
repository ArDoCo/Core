/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator;

import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;

/**
 * The Interface IInstanceLink defines a link between an {@link RecommendedInstance} and an {@link ModelInstance}.
 */
public interface InstanceLink extends ICopyable<InstanceLink> {

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
    void setProbability(Claimant claimant, double probability);

    /**
     * Returns the recommended instance.
     *
     * @return the textual instance
     */
    RecommendedInstance getTextualInstance();

    /**
     * Returns the model instance.
     *
     * @return the extracted instance
     */
    ModelInstance getModelInstance();

    /**
     * Returns all occurrences of all recommended instance names as string.
     *
     * @return all names of the recommended instances
     */
    String getNameOccurrencesAsString();

}
