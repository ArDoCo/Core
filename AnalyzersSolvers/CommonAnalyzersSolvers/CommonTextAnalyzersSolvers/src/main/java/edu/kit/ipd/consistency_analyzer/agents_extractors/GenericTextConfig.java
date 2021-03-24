package edu.kit.ipd.consistency_analyzer.agents_extractors;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class GenericTextConfig {

	public static final GenericTextConfig DEFAULT_CONFIG = new GenericTextConfig();

	public final List<String> textExtractors;

	// ArticleTypeNameAnalyzer
	/**
	 * The probability of the article type name analyzer.
	 */
	public final double articleTypeNameAnalyzerProbability;

	// InDepArcsAnalyzer
	/**
	 * The probability of the in dep arcs analyzer.
	 */
	public final double inDepArcsAnalyzerProbability;

	// MultiplePartSolver
	/**
	 * The probability of the multiple part solver.
	 */
	public final double multiplePartSolverProbability;

	// NounAnalyzer
	/**
	 * The probability of the noun analyzer.
	 */
	public final double nounAnalyzerProbability;

	// OutDepArcsAnalyzer
	/**
	 * The probability of the out dep arcs analyzer.
	 */
	public final double outDepArcsAnalyzerProbability;

	// SeparatedNamesAnalyzer
	/**
	 * The probability of the separated names analyzer.
	 */
	public final double separatedNamesAnalyzerProbability;

	private GenericTextConfig() {
		SystemParameters CONFIG = new SystemParameters("/configs/TextAnalyzerSolverConfig.properties", true);
		textExtractors = CONFIG.getPropertyAsList("Extractors");
		articleTypeNameAnalyzerProbability = CONFIG.getPropertyAsDouble("ArticleTypeNameAnalyzer_Probability");
		inDepArcsAnalyzerProbability = CONFIG.getPropertyAsDouble("InDepArcsAnalyzer_Probability");
		multiplePartSolverProbability = CONFIG.getPropertyAsDouble("MultiplePartSolver_Probability");
		nounAnalyzerProbability = CONFIG.getPropertyAsDouble("NounAnalyzer_Probability");
		outDepArcsAnalyzerProbability = CONFIG.getPropertyAsDouble("OutDepArcsAnalyzer_Probability");
		separatedNamesAnalyzerProbability = CONFIG.getPropertyAsDouble("SeparatedNamesAnalyzer_Probability");
	}

	public GenericTextConfig(List<String> textExtractors, double articleTypeNameAnalyzerProbability, double inDepArcsAnalyzerProbability, double multiplePartSolverProbability,
			double nounAnalyzerProbability, double outDepArcsAnalyzerProbability, double separatedNamesAnalyzerProbability) {
		this.textExtractors = textExtractors;
		this.articleTypeNameAnalyzerProbability = articleTypeNameAnalyzerProbability;
		this.inDepArcsAnalyzerProbability = inDepArcsAnalyzerProbability;
		this.multiplePartSolverProbability = multiplePartSolverProbability;
		this.nounAnalyzerProbability = nounAnalyzerProbability;
		this.outDepArcsAnalyzerProbability = outDepArcsAnalyzerProbability;
		this.separatedNamesAnalyzerProbability = separatedNamesAnalyzerProbability;
	}

}
