package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class GenericRecommendationAnalyzerSolverConfig {

	private GenericRecommendationAnalyzerSolverConfig() {
		throw new IllegalAccessError();
	}

	private static final SystemParameters CONFIG = loadParameters("/configs/RecommendationAnalyzerSolverConfig.properties");

	// ExtractionDependendOccurrenceAnalyzer
	/**
	 * The probability of the extraction dependent occurrence analyzer.
	 */
	public static final double EXTRACTION_DEPENDENT_OCCURRENCE_ANALYZER_PROBABILITY = CONFIG.getPropertyAsDouble("ExtractionDependentOccurrenceAnalyzer_Probability");

	// ExtractedTermsAnalyzer
	/**
	 * The probability for terms with an adjacent noun to be of that type or have
	 * that name.
	 */
	public static final double EXTRACTED_TERMS_ANALYZER_PROBABILITY_ADJACENT_NOUN = CONFIG.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityAdjacentNoun");
	/**
	 * The probability for terms with no adjacent nouns and therefore without type,
	 * to be recommended.
	 */
	public static final double EXTRACTED_TERMS_ANALYZER_PROBABILITY_JUST_NAME = CONFIG.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityJustName");
	/**
	 * The probability term combinations are recommended with.
	 */
	public static final double EXTRACTED_TERMS_ANALYZER_PROBABILITY_ADJACENT_TERM = CONFIG.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityAdjacentTerm");

	// NameTypeAnalyzer
	/**
	 * The probability of the name type analyzer.
	 */
	public static final double NAME_TYPE_ANALYZER_PROBABILITY = CONFIG.getPropertyAsDouble("NameTypeAnalyzer_Probability");

	// ReferenceSolver
	/**
	 * The probability of the reference solver.
	 */
	public static final double REFERENCE_SOLVER_PROBABILITY = CONFIG.getPropertyAsDouble("ReferenceSolver_Probability");
	/**
	 * The decrease of the reference solver.
	 */
	public static final double REFERENCE_SOLVER_PROPORTIONAL_DECREASE = CONFIG.getPropertyAsDouble("ReferenceSolver_ProportionalDecrease");

	/**
	 * The threshold for words similarities in the reference solver.
	 */
	public static final double REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD = CONFIG.getPropertyAsDouble("ReferenceSolver_areNamesSimilarThreshold");

	// SeparatedRelationSolver
	/**
	 * The probability of the separated relations solver.
	 */
	public static final double SEPARATED_RELATIONS_SOLVER_PROBABILITY = CONFIG.getPropertyAsDouble("SeparatedRelationsSolver_Probability");

	private static SystemParameters loadParameters(String filePath) {
		return new SystemParameters(filePath, true);
	}

}
