package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public class GenericRecommendationConfig extends Configuration {

    public static final GenericRecommendationConfig DEFAULT_CONFIG = new GenericRecommendationConfig();

    public final List<String> recommendationExtractors;

    // NameTypeAnalyzer
    /**
     * The probability of the name type analyzer.
     */
    public final double nameTypeAnalyzerProbability;

    // SeparatedRelationSolver
    /**
     * The probability of the separated relations solver.
     */
    public final double separatedRelationSolverProbility;

    public GenericRecommendationConfig() {
        SystemParameters config = new SystemParameters("/configs/RecommendationAnalyzerSolverConfig.properties", true);
        recommendationExtractors = config.getPropertyAsList("Recommendation_Extractors");
        nameTypeAnalyzerProbability = config.getPropertyAsDouble("NameTypeAnalyzer_Probability");
        separatedRelationSolverProbility = config.getPropertyAsDouble("SeparatedRelationsSolver_Probability");
    }

    public GenericRecommendationConfig(Map<String, String> configs) {
        recommendationExtractors = getPropertyAsList("Recommendation_Extractors", configs);
        nameTypeAnalyzerProbability = getPropertyAsDouble("NameTypeAnalyzer_Probability", configs);
        separatedRelationSolverProbility = getPropertyAsDouble("SeparatedRelationsSolver_Probability", configs);
    }

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(//
                "Recommendation_Extractors", String.join(" ", recommendationExtractors), //
                "NameTypeAnalyzer_Probability", String.valueOf(nameTypeAnalyzerProbability), //
                "SeparatedRelationsSolver_Probability", String.valueOf(separatedRelationSolverProbility) //
        );
    }

}
