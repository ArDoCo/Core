/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.ResourceAccessor;

/**
 * The Class GenericRecommendationConfig defines the configuration of the agents of this stage.
 */
public class GenericRecommendationConfig extends Configuration {

    private static final String SEPARATED_RELATIONS_SOLVER_PROBABILITY = "SeparatedRelationsSolver_Probability";
    private static final String NAME_TYPE_ANALYZER_PROBABILITY = "NameTypeAnalyzer_Probability";
    private static final String RECOMMENDATION_EXTRACTORS = "Recommendation_Extractors";
    private static final String DEPENDENCY_EXTRACTORS = "Dependency_Extractors";
    private static final String PHRASE_RECOMMENDATION_CONFIDENCE = "PhraseRecommendation_Confidence";

    /** The DEFAULT_CONFIG. */
    public static final GenericRecommendationConfig DEFAULT_CONFIG = new GenericRecommendationConfig();

    /** The recommendation extractors to be loaded. */
    public final ImmutableList<String> recommendationExtractors;

    public final ImmutableList<String> dependencyExtractors;

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

    /**
     * The confidence for phrase recommendations
     */
    public final double phraseRecommendationConfidence;

    /**
     * Instantiates a new generic recommendation config.
     */
    public GenericRecommendationConfig() {
        var config = new ResourceAccessor("/configs/RecommendationAnalyzerSolverConfig.properties", true);
        recommendationExtractors = config.getPropertyAsList(RECOMMENDATION_EXTRACTORS);
        dependencyExtractors = config.getPropertyAsList(DEPENDENCY_EXTRACTORS);
        nameTypeAnalyzerProbability = config.getPropertyAsDouble(NAME_TYPE_ANALYZER_PROBABILITY);
        separatedRelationSolverProbility = config.getPropertyAsDouble(SEPARATED_RELATIONS_SOLVER_PROBABILITY);
        phraseRecommendationConfidence = config.getPropertyAsDouble(PHRASE_RECOMMENDATION_CONFIDENCE);
    }

    /**
     * Instantiates a new generic recommendation config.
     *
     * @param configs the configs
     */
    public GenericRecommendationConfig(Map<String, String> configs) {
        recommendationExtractors = getPropertyAsList(RECOMMENDATION_EXTRACTORS, configs);
        dependencyExtractors = getPropertyAsList(DEPENDENCY_EXTRACTORS, configs);
        nameTypeAnalyzerProbability = getPropertyAsDouble(NAME_TYPE_ANALYZER_PROBABILITY, configs);
        separatedRelationSolverProbility = getPropertyAsDouble(SEPARATED_RELATIONS_SOLVER_PROBABILITY, configs);
        phraseRecommendationConfidence = getPropertyAsDouble(PHRASE_RECOMMENDATION_CONFIDENCE, configs);
    }

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(//
                RECOMMENDATION_EXTRACTORS, String.join(" ", recommendationExtractors), //
                NAME_TYPE_ANALYZER_PROBABILITY, String.valueOf(nameTypeAnalyzerProbability), //
                SEPARATED_RELATIONS_SOLVER_PROBABILITY, String.valueOf(separatedRelationSolverProbility), //
                DEPENDENCY_EXTRACTORS, String.valueOf(dependencyExtractors), //
                PHRASE_RECOMMENDATION_CONFIDENCE, String.valueOf(phraseRecommendationConfidence));
    }

}
