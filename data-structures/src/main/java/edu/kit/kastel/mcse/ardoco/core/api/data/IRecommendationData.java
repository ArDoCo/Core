/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;

public interface IRecommendationData extends IData {
    void setRecommendationState(Metamodel mm, IRecommendationState state);

    IRecommendationState getRecommendationState(Metamodel mm);
}
