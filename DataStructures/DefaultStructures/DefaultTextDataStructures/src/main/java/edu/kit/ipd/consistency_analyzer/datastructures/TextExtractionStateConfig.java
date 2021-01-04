package edu.kit.ipd.consistency_analyzer.datastructures;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public final class TextExtractionStateConfig {

	private TextExtractionStateConfig() {
		throw new IllegalAccessError();
	}

	private static final SystemParameters CONFIG = loadParameters("/configs/TextExtractionState.properties");
	/**
	 * The probability of the hardAdd method in the text extraction state.
	 */
	public static final double HARD_ADD_PROBABILITY = CONFIG.getPropertyAsDouble("hardAddProbability");

	private static SystemParameters loadParameters(String filePath) {
		return new SystemParameters(filePath, true);
	}
}
