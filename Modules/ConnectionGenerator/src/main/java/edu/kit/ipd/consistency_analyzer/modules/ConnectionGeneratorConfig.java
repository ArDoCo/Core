package edu.kit.ipd.consistency_analyzer.modules;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class ConnectionGeneratorConfig {

	private ConnectionGeneratorConfig() {
		throw new IllegalAccessError();
	}

	private static final SystemParameters CONFIG = loadParameters("/configs/ConnectionGenerator.properties");

	/**
	 * The list of solver types that should work on the connection state.
	 */
	protected static final List<String> CONNECTION_AGENTS = CONFIG.getPropertyAsList("Connection_Agents");

	private static SystemParameters loadParameters(String filePath) {
		return new SystemParameters(filePath, true);
	}
}
