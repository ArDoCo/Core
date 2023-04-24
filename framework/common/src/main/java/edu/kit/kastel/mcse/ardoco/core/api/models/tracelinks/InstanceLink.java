/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;

/**
 * An InstanceLink defines a link between an {@link RecommendedInstance} and an {@link ModelInstance}.
 */
public abstract class InstanceLink extends EndpointTuple {

    protected InstanceLink(RecommendedInstance textualInstance, ModelInstance modelInstance) {
        super(textualInstance, modelInstance);
    }

    /**
     * Returns the probability of the correctness of this link.
     *
     * @return the probability of this link
     */
    public abstract double getProbability();

    /**
     * Returns the recommended instance.
     *
     * @return the textual instance
     */
    public abstract RecommendedInstance getTextualInstance();

    /**
     * Returns the model instance.
     *
     * @return the extracted instance
     */
    public abstract ModelInstance getModelInstance();

}
