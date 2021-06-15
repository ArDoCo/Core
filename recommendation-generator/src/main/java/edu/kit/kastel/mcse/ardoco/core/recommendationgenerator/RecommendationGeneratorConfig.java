package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public class RecommendationGeneratorConfig extends Configuration {

    private static final String RECOMMENDATION_AGENTS = "Recommendation_Agents";
    public static final RecommendationGeneratorConfig DEFAULT_CONFIG = new RecommendationGeneratorConfig();

    private RecommendationGeneratorConfig() {
        var config = new SystemParameters("/configs/RecommendationGenerator.properties", true);
        recommendationAgents = config.getPropertyAsList(RECOMMENDATION_AGENTS);
    }

    public RecommendationGeneratorConfig(Map<String, String> configs) {
        recommendationAgents = getPropertyAsList(RECOMMENDATION_AGENTS, configs);
    }

    /**
     * The list of analyzer types that should work on the recommendation state.
     */
    public final List<String> recommendationAgents;

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(RECOMMENDATION_AGENTS, String.join(" ", recommendationAgents));
    }

}
