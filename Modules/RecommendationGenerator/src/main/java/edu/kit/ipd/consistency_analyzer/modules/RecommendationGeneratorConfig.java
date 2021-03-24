package edu.kit.ipd.consistency_analyzer.modules;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class RecommendationGeneratorConfig {

	public static final RecommendationGeneratorConfig DEFAULT_CONFIG = new RecommendationGeneratorConfig();

	private RecommendationGeneratorConfig() {
		SystemParameters config = new SystemParameters("/configs/RecommendationGenerator.properties", true);
		recommendationAgents = config.getPropertyAsList("Recommendation_Agents");
	}

	public RecommendationGeneratorConfig(List<String> recommendation) {
		recommendationAgents = recommendation;
	}

	/**
	 * The list of analyzer types that should work on the recommendation state.
	 */
	public final List<String> recommendationAgents;

}
