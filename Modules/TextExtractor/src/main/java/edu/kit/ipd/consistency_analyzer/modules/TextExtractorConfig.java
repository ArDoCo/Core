package edu.kit.ipd.consistency_analyzer.modules;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class TextExtractorConfig {

	private TextExtractorConfig() {
		throw new IllegalAccessError();
	}

	private static final SystemParameters CONFIG = loadParameters("/configs/TextExtractor.properties");

	/**
	 * The list of text extraction agent types that should run.
	 */
	public static final List<String> TEXT_EXTRACTION_AGENT_ANALYZERS = CONFIG.getPropertyAsList("TextExtractionAgent_Analyzers");

	/**
	 * The list of text extraction solver types that should run.
	 */
	public static final List<String> TEXT_EXTRACTION_AGENT_SOLVERS = CONFIG.getPropertyAsList("TextExtractionAgent_Solvers");

	private static SystemParameters loadParameters(String filePath) {
		return new SystemParameters(filePath, true);
	}
}