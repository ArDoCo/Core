/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator;

import edu.kit.kastel.informalin.framework.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

public interface RecommendationStates extends PipelineStepData {
    String ID = "RecommendationStates";

    RecommendationState getRecommendationState(Metamodel mm);
}
