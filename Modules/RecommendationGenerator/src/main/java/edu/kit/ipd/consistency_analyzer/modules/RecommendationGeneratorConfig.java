package edu.kit.ipd.consistency_analyzer.modules;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class RecommendationGeneratorConfig {

    private RecommendationGeneratorConfig() {
        throw new IllegalAccessError();
    }

    private static final SystemParameters CONFIG = loadParameters("/configs/RecommendationGenerator.properties");

    /**
     * The list of analyzer types that should work on the recommendation state.
     */
    protected static final List<String> RECOMMENDATION_AGENTS = CONFIG.getPropertyAsList("Recommendation_Agents");

    private static SystemParameters loadParameters(String filePath) {
        return new SystemParameters(filePath, true);
    }

}
