package edu.kit.ipd.consistency_analyzer.modules;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class ConnectionGeneratorConfig {

	private ConnectionGeneratorConfig() {
		throw new IllegalAccessError();
	}

	private static final SystemParameters CONFIG = loadParameters("/configs/ConnectionGenerator.properties");

	/**
	 * The list of analyzer types that should work on the connection state.
	 */
	protected static final List<String> MODEL_CONNECTION_AGENT_ANALYZERS = CONFIG.getPropertyAsList("ModelConnectionAgent_Analyzers");
	/**
	 * The list of solver types that should work on the connection state.
	 */
	protected static final List<String> MODEL_CONNECTION_AGENT_SOLVERS = CONFIG.getPropertyAsList("ModelConnectionAgent_Solvers");

	private static SystemParameters loadParameters(String filePath) {
		return new SystemParameters(filePath, true);
	}
}
