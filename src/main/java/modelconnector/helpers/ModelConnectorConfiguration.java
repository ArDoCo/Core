package modelconnector.helpers;

import java.util.List;

import modelconnector.connectionGenerator.analyzers.ModelConnectionAnalyzerType;
import modelconnector.connectionGenerator.solvers.ModelConnectionSolverType;
import modelconnector.recommendationGenerator.analyzers.RecommendationAnalyzerType;
import modelconnector.recommendationGenerator.solvers.RecommendationSolverType;
import modelconnector.textExtractor.analyzers.TextExtractionAnalyzerType;
import modelconnector.textExtractor.solvers.TextExtractionSolverType;

/**
 * This class loads the configurations in final attributes.
 *
 * @author Sophie
 *
 */
public final class ModelConnectorConfiguration {

    private ModelConnectorConfiguration() {
        throw new IllegalAccessError();
    }

    // Starter:
    /**
     * The path to read the textual input (documentation) from.
     */
    public static final String DOCUMENTATION_PATH = Configurator.getProperty("documentation_Path");

    /**
     * The path to a text textual input.
     */
    public static final String TEST_DOCUMENTATION_PATH = Configurator.getProperty("testDocumentation_Path");
    /**
     * The path to write the read in input in.
     */
    public static final String FILE_FOR_INPUT_PATH = Configurator.getProperty("fileForInput_Path");

    /**
     * The path to write the results in.
     */
    public static final String FILE_FOR_RESULTS_PATH = Configurator.getProperty("fileForResults_Path");

    // SimilarityUtils:
    /**
     * List of separators used for containing by a word in SimilarityUtils.
     */
    public static final List<String> SEPARATORS_TO_CONTAIN = Configurator.getPropertyAsList("separators_ToContain");
    /**
     * List of separators used for splitting a word in SimilarityUtils.
     */
    public static final List<String> SEPARATORS_TO_SPLIT = Configurator.getPropertyAsList("separators_ToSplit");

    /**
     * Int for the minimal length of word similarity for methods in SimilarityUtils.
     */
    public static final int ARE_WORDS_SIMILAR_MIN_LENGTH = Configurator.getPropertyAsInt("areWordsSimilar_MinLength");
    /**
     * Int for the maximal levensthein distance for two words to be similar for methods in SimilarityUtils.
     */
    public static final int ARE_WORDS_SIMILAR_MAX_L_DIST = Configurator.getPropertyAsInt("areWordsSimilar_MaxLdist");
    /**
     * The default threshold for similarity in methods of SimilarityUtils.
     */
    public static final double ARE_WORDS_SIMILAR_DEFAULT_THRESHOLD = Configurator.getPropertyAsDouble(
            "areWordsSimilar_DefaultThreshold");

    /**
     * Threshold for the similarity of two words in the similarity function of two lists.
     */
    public static final double ARE_WORDS_OF_LISTS_SIMILAR_WORD_SIMILARITY_THRESHOLD = Configurator.getPropertyAsDouble(
            "areWordsOfListsSimilar_WordSimilarityThreshold");

    /**
     * Default threshold for the similarity function of two lists.
     */
    public static final double ARE_WORDS_OF_LISTS_SIMILAR_DEFAULT_THRESHOLD = Configurator.getPropertyAsDouble(
            "areWordsOfListsSimilar_DefaultThreshold");
    /**
     * The minimal propotion of two lists that need to be similar, that both are similar. Used in SimilarityUtils.
     */
    public static final double GET_MOST_RECOMMENDED_I_BY_REF_MIN_PROPORTION = Configurator.getPropertyAsDouble(
            "getMostRecommendedIByRef_MinProportion");
    /**
     * The increase for the method getMostRecommendedInstancesByReference in SimilarityUtils.
     */
    public static final double GET_MOST_RECOMMENDED_I_BY_REF_INCREASE = Configurator.getPropertyAsDouble(
            "getMostRecommendedIByRef_Increase");

    /**
     * The threshold for the method getMostLikelyMappingByReference in SimilarityUtils.
     */
    public static final double GET_MOST_LIKELY_MP_BY_REFERENCE_THRESHOLD = Configurator.getPropertyAsDouble(
            "getMostLikelyMpByReference_Threshold");
    /**
     * The increase for the method getMostLikelyMappingByReference in SimilarityUtils.
     */
    public static final double GET_MOST_LIKELY_MP_BY_REFERENCE_INCREASE = Configurator.getPropertyAsDouble(
            "getMostLikelyMpBReference_Increase");

    // TextExtractionAgent:
    /**
     * The list of text extraction agent types that should run.
     */
    public static final List<TextExtractionAnalyzerType> TEXT_EXTRACTION_AGENT_ANALYZERS = //
            Configurator.getPropertyAsListOfEnumTypes("TextExtractionAgent_Analyzers",
                    TextExtractionAnalyzerType.class);

    /**
     * The list of text extraction solver types that should run.
     */
    public static final List<TextExtractionSolverType> TEXT_EXTRACTION_AGENT_SOLVERS = //
            Configurator.getPropertyAsListOfEnumTypes("TextExtractionAgent_Solvers", TextExtractionSolverType.class);

    // TextExtractionState
    /**
     * The probability of the hardAdd method in the text extraction state.
     */
    public static final double TEXT_EXTRACTION_STATE_HARD_ADD_PROBABILITY = Configurator.getPropertyAsDouble(
            "TextExtractionState_hardAddProbability");

    // SeparatedNamesAnalyzer
    /**
     * The probability of the separated names analyzer.
     */
    public static final double SEPARATED_NAMES_ANALYZER_PROBABILITY = Configurator.getPropertyAsDouble(
            "SeparatedNamesAnalyzer_Probability");

    // OutDepArcsAnalyzer
    /**
     * The probability of the out dep arcs analyzer.
     */
    public static final double OUT_DEP_ARCS_ANALYZER_PROBABILITY = Configurator.getPropertyAsDouble(
            "OutDepArcsAnalyzer_Probability");

    // NounAnalyzer
    /**
     * The probability of the noun analyzer.
     */
    public static final double NOUN_ANALYZER_PROBABILITY = Configurator.getPropertyAsDouble("NounAnalyzer_Probability");

    // MultiplePartSolver
    /**
     * The probability of the multiple part solver.
     */
    public static final double MULTIPLE_PART_SOLVER_PROBABILITY = Configurator.getPropertyAsDouble(
            "MultiplePartSolver_Probability");

    // InDepArcsAnalyzer
    /**
     * The probability of the in dep arcs analyzer.
     */
    public static final double IN_DEP_ARCS_ANALYZER_PROBABILITY = Configurator.getPropertyAsDouble(
            "InDepArcsAnalyzer_Probability");

    // ArticleTypeNameAnalyzer
    /**
     * The probability of the article type name analyzer.
     */
    public static final double ARTICLE_TYPE_NAME_ANALYZER_PROBABILITY = Configurator.getPropertyAsDouble(
            "ArticleTypeNameAnalyzer_Probability");

    // ModelExtractionState
    /**
     * The minimal amount of parts of the type that the type is splitted and can be identified by parts.
     */
    public static final int EXTRACTION_STATE_MIN_TYPE_PARTS = Configurator.getPropertyAsInt(
            "ExtractionState_MinTypeParts");

    // RecommendationAgent
    /**
     * The list of analyzer types that should work on the recommendation state.
     */
    public static final List<RecommendationAnalyzerType> RECOMMENDATION_AGENT_ANALYZERS = //
            Configurator.getPropertyAsListOfEnumTypes("RecommendationAgent_Analyzers",
                    RecommendationAnalyzerType.class);

    /**
     * The list of solver types that should work on the recommendation state.
     */
    public static final List<RecommendationSolverType> RECOMMENDATION_AGENT_SOLVERS = //
            Configurator.getPropertyAsListOfEnumTypes("RecommendationAgent_Solvers", RecommendationSolverType.class);

    // ModelConnectionAgent
    /**
     * The list of analyzer types that should work on the connection state.
     */
    public static final List<ModelConnectionAnalyzerType> MODEL_CONNECTION_AGENT_ANALYZERS = //
            Configurator.getPropertyAsListOfEnumTypes("ModelConnectionAgent_Analyzers",
                    ModelConnectionAnalyzerType.class);
    /**
     * The list of solver types that should work on the connection state.
     */
    public static final List<ModelConnectionSolverType> MODEL_CONNECTION_AGENT_SOLVERS = //
            Configurator.getPropertyAsListOfEnumTypes("ModelConnectionAgent_Solvers", ModelConnectionSolverType.class);

    // SeparatedRelationSolver
    /**
     * The probability of the separated relations solver.
     */
    public static final double SEPARATED_RELATIONS_SOLVER_PROBABILITY = Configurator.getPropertyAsDouble(
            "SeparatedRelationsSolver_Probability");

    // ExtractedTermsAnalyzer
    /**
     * The probability for terms with an adjacent noun to be of that type or have that name.
     */
    public static final double EXTRACTED_TERMS_ANALYZER_PROBABILITY_ADJACENT_NOUN = Configurator.getPropertyAsDouble(
            "ExtractedTermsAnalyzer_ProbabilityAdjacentNoun");
    /**
     * The probability for terms with no adjacent nouns and therefore without type, to be recommended.
     */
    public static final double EXTRACTED_TERMS_ANALYZER_PROBABILITY_JUST_NAME = Configurator.getPropertyAsDouble(
            "ExtractedTermsAnalyzer_ProbabilityJustName");
    /**
     * The probability term combinations are recommended with.
     */
    public static final double EXTRACTED_TERMS_ANALYZER_PROBABILITY_ADJACENT_TERM = Configurator.getPropertyAsDouble(
            "ExtractedTermsAnalyzer_ProbabilityAdjacentTerm");

    // ReferenceSolver
    /**
     * The probability of the reference solver.
     */
    public static final double REFERENCE_SOLVER_PROBABILITY = Configurator.getPropertyAsDouble(
            "ReferenceSolver_Probability");
    /**
     * The decrease of the reference solver.
     */
    public static final double REFERENCE_SOLVER_PROPORTIONAL_DECREASE = Configurator.getPropertyAsDouble(
            "ReferenceSolver_ProportionalDecrease");

    /**
     * The threshold for words similarities in the reference solver.
     */
    public static final double REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD = Configurator.getPropertyAsDouble(
            "ReferenceSolver_areNamesSimilarThreshold");

    // RelationConnectionSolver
    /**
     * The probability of the relation connection solver.
     */
    public static final double RELATION_CONNECTION_SOLVER_PROBABILITY = Configurator.getPropertyAsDouble(
            "RelationConnectionSolver_Probability");

    // NameTypeAnalyzer
    /**
     * The probability of the name type analyzer.
     */
    public static final double NAME_TYPE_ANALYZER_PROBABILITY = Configurator.getPropertyAsDouble(
            "NameTypeAnalyzer_Probability");

    // InstanceMappingConnectionSolver
    /**
     * The probability of the instance mapping connection solver.
     */
    public static final double INSTANCE_CONNECTION_SOLVER_PROBABILITY = Configurator.getPropertyAsDouble(
            "InstanceConnectionSolver_Probability");
    /**
     * The probability of the instance mapping connection solver, if the connection does not include the comparison of a
     * type.
     */
    public static final double INSTANCE_CONNECTION_SOLVER_PROBABILITY_WITHOUT_TYPE = Configurator.getPropertyAsDouble(
            "InstanceConnectionSolver_ProbabilityWithoutType");
    // ExtractionDependendOccurrenceAnalyzer
    /**
     * The probability of the extraction dependent occurrence analyzer.
     */
    public static final double EXTRACTION_DEPENDENT_OCCURRENCE_ANALYZER_PROBABILITY = Configurator.getPropertyAsDouble(
            "ExtractionDependentOccurrenceAnalyzer_Probability");

}
