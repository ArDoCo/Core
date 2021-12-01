/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.ResourceAccessor;

/**
 * The Class RecommendationGeneratorConfig defines the configuration of this stage.
 */
public class RecommendationGeneratorConfig extends Configuration {
    private static final String RECOMMENDATION_AGENTS = "Recommendation_Agents";
    private static final String DEPENDENCY_AGENTS = "Dependency_Agents";

    /** The Constant DEFAULT_CONFIG. */
    public static final RecommendationGeneratorConfig DEFAULT_CONFIG = new RecommendationGeneratorConfig();

    private RecommendationGeneratorConfig() {

        var config = new ResourceAccessor("/configs/RecommendationGenerator.properties", true);
        recommendationAgents = config.getPropertyAsList(RECOMMENDATION_AGENTS);
        dependencyAgents = config.getPropertyAsList(DEPENDENCY_AGENTS);
    }

    /**
     * Instantiates a new recommendation generator config.
     *
     * @param configs the configs
     */
    public RecommendationGeneratorConfig(Map<String, String> configs) {
        recommendationAgents = getPropertyAsList(RECOMMENDATION_AGENTS, configs);
        dependencyAgents = getPropertyAsList(DEPENDENCY_AGENTS, configs);
    }

    /**
     * The list of analyzer types that should work on the recommendation state.
     */
    public final ImmutableList<String> recommendationAgents;
    /**
     * The list of analyzer types that should work on the dependency state.
     */
    public final ImmutableList<String> dependencyAgents;

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(RECOMMENDATION_AGENTS, String.join(" ", recommendationAgents), DEPENDENCY_AGENTS, String.join(" ", dependencyAgents));
    }

}
