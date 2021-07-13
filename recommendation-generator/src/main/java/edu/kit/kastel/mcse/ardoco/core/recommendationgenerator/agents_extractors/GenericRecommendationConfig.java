package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public class GenericRecommendationConfig extends Configuration {

    private static final String SEPARATED_RELATIONS_SOLVER_PROBABILITY = "SeparatedRelationsSolver_Probability";
    private static final String NAME_TYPE_ANALYZER_PROBABILITY = "NameTypeAnalyzer_Probability";
    private static final String RECOMMENDATION_EXTRACTORS = "Recommendation_Extractors";
    private static final String DEPENDENCY_EXTRACTORS = "Dependency_Extractors";

    public static final GenericRecommendationConfig DEFAULT_CONFIG = new GenericRecommendationConfig();

    public final List<String> recommendationExtractors;

    public final List<String> dependencyExtractors;

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
        var config = new SystemParameters("/configs/RecommendationAnalyzerSolverConfig.properties", true);
        recommendationExtractors = config.getPropertyAsList(RECOMMENDATION_EXTRACTORS);
        nameTypeAnalyzerProbability = config.getPropertyAsDouble(NAME_TYPE_ANALYZER_PROBABILITY);
        separatedRelationSolverProbility = config.getPropertyAsDouble(SEPARATED_RELATIONS_SOLVER_PROBABILITY);
        dependencyExtractors = config.getPropertyAsList(DEPENDENCY_EXTRACTORS);

    }

    public GenericRecommendationConfig(Map<String, String> configs) {
        recommendationExtractors = getPropertyAsList(RECOMMENDATION_EXTRACTORS, configs);
        nameTypeAnalyzerProbability = getPropertyAsDouble(NAME_TYPE_ANALYZER_PROBABILITY, configs);
        separatedRelationSolverProbility = getPropertyAsDouble(SEPARATED_RELATIONS_SOLVER_PROBABILITY, configs);
        dependencyExtractors = getPropertyAsList(DEPENDENCY_EXTRACTORS, configs);
    }

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(//
                RECOMMENDATION_EXTRACTORS, String.join(" ", recommendationExtractors), //
                NAME_TYPE_ANALYZER_PROBABILITY, String.valueOf(nameTypeAnalyzerProbability), //
                SEPARATED_RELATIONS_SOLVER_PROBABILITY, String.valueOf(separatedRelationSolverProbility), //
                DEPENDENCY_EXTRACTORS, String.valueOf(dependencyExtractors)
        );
    }

}
