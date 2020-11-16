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
	public static final String documentation_Path = Configurator.getProperty("documentation_Path");

	/**
	 * The path to a text textual input.
	 */
	public static final String testDocumentation_Path = Configurator.getProperty("testDocumentation_Path");
	/**
	 * The path to write the read in input in.
	 */
	public static final String fileForInput_Path = Configurator.getProperty("fileForInput_Path");

	/**
	 * The path to write the results in.
	 */
	public static final String fileForResults_Path = Configurator.getProperty("fileForResults_Path");

	// SimilarityUtils:
	/**
	 * List of separators used for containing by a word in SimilarityUtils.
	 */
	public static final List<String> separators_toContain = Configurator.getPropertyAsList("separators_ToContain");
	/**
	 * List of separators used for splitting a word in SimilarityUtils.
	 */
	public static final List<String> separators_toSplit = Configurator.getPropertyAsList("separators_ToSplit");

	/**
	 * Int for the minimal length of word similarity for methods in SimilarityUtils.
	 */
	public static final int areWordsSimilar_MinLenght = Configurator.getPropertyAsInt("areWordsSimilar_MinLength");
	/**
	 * Int for the maximal levensthein distance for two words to be similar for
	 * methods in SimilarityUtils.
	 */
	public static final int areWordsSimilar_MaxLDist = Configurator.getPropertyAsInt("areWordsSimilar_MaxLdist");
	/**
	 * The default threshold for similarity in methods of SimilarityUtils.
	 */
	public static final double areWordsSimilar_DefaultThreshold = Configurator.getPropertyAsDouble("areWordsSimilar_DefaultThreshold");

	/**
	 * Threshold for the similarity of two words in the similarity function of two
	 * lists.
	 */
	public static final double areWordsOfListsSimilar_WordSimilarityThreshold = Configurator.getPropertyAsDouble("areWordsOfListsSimilar_WordSimilarityThreshold");

	/**
	 * Default threshold for the similarity function of two lists.
	 */
	public static final double areWordsOfListsSimilar_DefaultThreshold = Configurator.getPropertyAsDouble("areWordsOfListsSimilar_DefaultThreshold");
	/**
	 * The minimal propotion of two lists that need to be similar, that both are
	 * similar. Used in SimilarityUtils.
	 */
	public static final double getMostRecommendedIByRef_MinProportion = Configurator.getPropertyAsDouble("getMostRecommendedIByRef_MinProportion");
	/**
	 * The increase for the method getMostRecommendedInstancesByReference in
	 * SimilarityUtils.
	 */
	public static final double getMostRecommendedIByref_Increase = Configurator.getPropertyAsDouble("getMostRecommendedIByRef_Increase");

	/**
	 * The threshold for the method getMostLikelyMappingByReference in
	 * SimilarityUtils.
	 */
	public static final double getMostLikelyMpByReference_Threshold = Configurator.getPropertyAsDouble("getMostLikelyMpByReference_Threshold");
	/**
	 * The increase for the method getMostLikelyMappingByReference in
	 * SimilarityUtils.
	 */
	public static final double getMostLikelyMpByReference_Increase = Configurator.getPropertyAsDouble("getMostLikelyMpBReference_Increase");

	// TextExtractionAgent:
	/**
	 * The list of text extraction agent types that should run.
	 */
	public static final List<TextExtractionAnalyzerType> textExtractionAgent_Analyzers = //
			Configurator.getPropertyAsListOfEnumTypes("TextExtractionAgent_Analyzers", TextExtractionAnalyzerType.class);

	/**
	 * The list of text extraction solver types that should run.
	 */
	public static final List<TextExtractionSolverType> textExtractionAgemt_Solvers = //
			Configurator.getPropertyAsListOfEnumTypes("TextExtractionAgent_Solvers", TextExtractionSolverType.class);

	// TextExtractionState
	/**
	 * The probability of the hardAdd method in the text extraction state.
	 */
	public static final double textExtractionState_hardAddProbability = Configurator.getPropertyAsDouble("TextExtractionState_hardAddProbability");

	// SeparatedNamesAnalyzer
	/**
	 * The probability of the separated names analyzer.
	 */
	public static final double separatedNamesAnalyzer_Probability = Configurator.getPropertyAsDouble("SeparatedNamesAnalyzer_Probability");

	// OutDepArcsAnalyzer
	/**
	 * The probability of the out dep arcs analyzer.
	 */
	public static final double outDepArcsAnalyzer_Probability = Configurator.getPropertyAsDouble("OutDepArcsAnalyzer_Probability");

	// NounAnalyzer
	/**
	 * The probability of the noun analyzer.
	 */
	public static final double nounAnalyzer_Probability = Configurator.getPropertyAsDouble("NounAnalyzer_Probability");

	// MultiplePartSolver
	/**
	 * The probability of the multiple part solver.
	 */
	public static final double multiplePartSolver_Probability = Configurator.getPropertyAsDouble("MultiplePartSolver_Probability");

	// InDepArcsAnalyzer
	/**
	 * The probability of the in dep arcs analyzer.
	 */
	public static final double inDepArcsAnalyzer_Probability = Configurator.getPropertyAsDouble("InDepArcsAnalyzer_Probability");

	// ArticleTypeNameAnalyzer
	/**
	 * The probability of the article type name analyzer.
	 */
	public static final double articleTypeNameAnalyzer_Probability = Configurator.getPropertyAsDouble("ArticleTypeNameAnalyzer_Probability");

	// ModelExtractionState
	/**
	 * The minimal amount of parts of the type that the type is splitted and can be
	 * identified by parts.
	 */
	public static final int extractionState_MinTypeParts = Configurator.getPropertyAsInt("ExtractionState_MinTypeParts");

	// RecommendationAgent
	/**
	 * The list of analyzer types that should work on the recommendation state.
	 */
	public static final List<RecommendationAnalyzerType> recommendationAgent_Analyzers = //
			Configurator.getPropertyAsListOfEnumTypes("RecommendationAgent_Analyzers", RecommendationAnalyzerType.class);

	/**
	 * The list of solver types that should work on the recommendation state.
	 */
	public static final List<RecommendationSolverType> recommendationAgent_Solvers = //
			Configurator.getPropertyAsListOfEnumTypes("RecommendationAgent_Solvers", RecommendationSolverType.class);

	// ModelConnectionAgent
	/**
	 * The list of analyzer types that should work on the connection state.
	 */
	public static final List<ModelConnectionAnalyzerType> modelConnectionAgent_Analyzers = //
			Configurator.getPropertyAsListOfEnumTypes("ModelConnectionAgent_Analyzers", ModelConnectionAnalyzerType.class);
	/**
	 * The list of solver types that should work on the connection state.
	 */
	public static final List<ModelConnectionSolverType> modelConnectionAgent_Solvers = //
			Configurator.getPropertyAsListOfEnumTypes("ModelConnectionAgent_Solvers", ModelConnectionSolverType.class);

	// SeparatedRelationSolver
	/**
	 * The probability of the separated relations solver.
	 */
	public static final double separatedRelationsSolver_Probability = Configurator.getPropertyAsDouble("SeparatedRelationsSolver_Probability");

	// ExtractedTermsAnalyzer
	/**
	 * The probability for terms with an adjacent noun to be of that type or have
	 * that name.
	 */
	public static final double extractedTermsAnalyzer_ProbabilityAdjacentNoun = Configurator.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityAdjacentNoun");
	/**
	 * The probability for terms with no adjacent nouns and therefore without type,
	 * to be recommended.
	 */
	public static final double extractedTermsAnalyzer_ProbabilityJustName = Configurator.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityJustName");
	/**
	 * The probability term combinations are recommended with.
	 */
	public static final double extractedTermsAnalyzer_ProbabilityAdjacentTerm = Configurator.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityAdjacentTerm");

	// ReferenceSolver
	/**
	 * The probability of the reference solver.
	 */
	public static final double referenceSolver_Probability = Configurator.getPropertyAsDouble("ReferenceSolver_Probability");
	/**
	 * The decrease of the reference solver.
	 */
	public static final double referenceSolver_ProportionalDecrease = Configurator.getPropertyAsDouble("ReferenceSolver_ProportionalDecrease");

	/**
	 * The threshold for words similarities in the reference solver.
	 */
	public static final double referenceSolver_AreNamesSimilarThreshold = Configurator.getPropertyAsDouble("ReferenceSolver_areNamesSimilarThreshold");

	// RelationConnectionSolver
	/**
	 * The probability of the relation connection solver.
	 */
	public static final double relationConnectionSolver_Probability = Configurator.getPropertyAsDouble("RelationConnectionSolver_Probability");

	// NameTypeAnalyzer
	/**
	 * The probability of the name type analyzer.
	 */
	public static final double nameTypeAnalyzerProbability = Configurator.getPropertyAsDouble("NameTypeAnalyzer_Probability");

	// InstanceMappingConnectionSolver
	/**
	 * The probability of the instance mapping connection solver.
	 */
	public static final double instanceConnectionSolver_Probability = Configurator.getPropertyAsDouble("InstanceConnectionSolver_Probability");
	/**
	 * The probability of the instance mapping connection solver, if the connection
	 * does not include the comparison of a type.
	 */
	public static final double instanceConnectionSolver_ProbabilityWithoutType = Configurator.getPropertyAsDouble("InstanceConnectionSolver_ProbabilityWithoutType");
//ExtractionDependendOccurrenceAnalyzer
	/**
	 * The probability of the extraction dependent occurrence analyzer.
	 */
	public static final double extractionDependentOccurrenceAnalyzer_Probability = Configurator.getPropertyAsDouble("ExtractionDependentOccurrenceAnalyzer_Probability");

}
