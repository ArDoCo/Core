package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public class RecommendationGeneratorConfig extends Configuration {

    public static final RecommendationGeneratorConfig DEFAULT_CONFIG = new RecommendationGeneratorConfig();

    private RecommendationGeneratorConfig() {
        SystemParameters config = new SystemParameters("/configs/RecommendationGenerator.properties", true);
        recommendationAgents = config.getPropertyAsList("Recommendation_Agents");
        dependencyAgents = config.getPropertyAsList("Dependency_Agents");
    }

    public RecommendationGeneratorConfig(Map<String, String> configs) {
        recommendationAgents = getPropertyAsList("Recommendation_Agents", configs);
        dependencyAgents = getPropertyAsList("Dependency_Agents", configs);
    }

    /**
     * The list of analyzer types that should work on the recommendation state.
     */
    public final List<String> recommendationAgents;
    /**
     * The list of analyzer types that should work on the dependency state.
     */
    public final List<String> dependencyAgents;

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of("Recommendation_Agents",
                String.join(" ", recommendationAgents) + " " + String.join(" ", dependencyAgents));
    }

}
