package edu.kit.ipd.consistency_analyzer.modules;

import java.util.List;
import java.util.Map;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Configuration;
import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class RecommendationGeneratorConfig extends Configuration {

    public static final RecommendationGeneratorConfig DEFAULT_CONFIG = new RecommendationGeneratorConfig();

    private RecommendationGeneratorConfig() {
        SystemParameters config = new SystemParameters("/configs/RecommendationGenerator.properties", true);
        recommendationAgents = config.getPropertyAsList("Recommendation_Agents");
    }

    public RecommendationGeneratorConfig(Map<String, String> configs) {
        recommendationAgents = getPropertyAsList("Recommendation_Agents", configs);
    }

    /**
     * The list of analyzer types that should work on the recommendation state.
     */
    public final List<String> recommendationAgents;

}
