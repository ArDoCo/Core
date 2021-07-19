package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.ResourceAccessor;

/**
 * The Class RecommendationGeneratorConfig defines the configuration of this stage.
 */
public class RecommendationGeneratorConfig extends Configuration {
    private static final String RECOMMENDATION_AGENTS = "Recommendation_Agents";

    /** The Constant DEFAULT_CONFIG. */
    public static final RecommendationGeneratorConfig DEFAULT_CONFIG = new RecommendationGeneratorConfig();

    private RecommendationGeneratorConfig() {
        var config = new ResourceAccessor("/configs/RecommendationGenerator.properties", true);
        recommendationAgents = config.getPropertyAsList(RECOMMENDATION_AGENTS);
    }

    /**
     * Instantiates a new recommendation generator config.
     *
     * @param configs the configs
     */
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
