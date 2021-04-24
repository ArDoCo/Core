package edu.kit.kastel.mcse.ardoco.core.datastructures;

import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public final class TextExtractionStateConfig {

	private TextExtractionStateConfig() {
		throw new IllegalAccessError();
	}

	private static final SystemParameters CONFIG = loadParameters("/configs/TextExtractionState.properties");
	/**
	 * The probability of the hardAdd method in the text extraction state.
	 */
	public static final double HARD_ADD_PROBABILITY = CONFIG.getPropertyAsDouble("hardAddProbability");

	public static final double NORT_PROBABILITY_FOR_NAME_AND_TYPE = CONFIG.getPropertyAsDouble("nortProbabilityForNameAndType");

	private static SystemParameters loadParameters(String filePath) {
		return new SystemParameters(filePath, true);
	}

}
