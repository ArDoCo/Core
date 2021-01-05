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
	protected static final List<String> RECOMMENDATION_AGENT_ANALYZERS = CONFIG.getPropertyAsList("RecommendationAgent_Analyzers");

	/**
	 * The list of solver types that should work on the recommendation state.
	 */
	protected static final List<String> RECOMMENDATION_AGENT_SOLVERS = CONFIG.getPropertyAsList("RecommendationAgent_Solvers");

	private static SystemParameters loadParameters(String filePath) {
		return new SystemParameters(filePath, true);
	}

}
