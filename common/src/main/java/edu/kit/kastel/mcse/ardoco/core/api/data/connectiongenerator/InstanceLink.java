/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;

/**
 * The Interface IInstanceLink defines a link between an {@link RecommendedInstance} and an {@link ModelInstance}.
 */
public interface InstanceLink {

    /**
     * Returns the probability of the correctness of this link.
     *
     * @return the probability of this link
     */
    double getProbability();

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

}
