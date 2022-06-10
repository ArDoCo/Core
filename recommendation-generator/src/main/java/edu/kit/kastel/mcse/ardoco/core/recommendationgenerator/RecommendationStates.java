package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.EnumMap;
import java.util.Map;

import edu.kit.kastel.informalin.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationStates;

public class RecommendationStates implements PipelineStepData, IRecommendationStates {
    private Map<Metamodel, RecommendationState> recommendationStates;

    private RecommendationStates() {
        recommendationStates = new EnumMap<>(Metamodel.class);
    }

    static public IRecommendationStates build() {
        var recStates = new RecommendationStates();
        for (Metamodel mm : Metamodel.values()) {
            recStates.recommendationStates.put(mm, new RecommendationState());
        }
        return recStates;
    }

    @Override
    public RecommendationState getRecommendationState(Metamodel mm) {
        return recommendationStates.get(mm);
    }
}
