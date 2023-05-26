/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.EnumMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationStates;

public class RecommendationStatesImpl implements RecommendationStates {
    private Map<Metamodel, RecommendationStateImpl> recommendationStates;

    private RecommendationStatesImpl() {
        recommendationStates = new EnumMap<>(Metamodel.class);
    }

    public static RecommendationStates build() {
        var recStates = new RecommendationStatesImpl();
        for (Metamodel mm : Metamodel.values()) {
            recStates.recommendationStates.put(mm, new RecommendationStateImpl());
        }
        return recStates;
    }

    @Override
    public RecommendationStateImpl getRecommendationState(Metamodel mm) {
        return recommendationStates.get(mm);
    }
}
