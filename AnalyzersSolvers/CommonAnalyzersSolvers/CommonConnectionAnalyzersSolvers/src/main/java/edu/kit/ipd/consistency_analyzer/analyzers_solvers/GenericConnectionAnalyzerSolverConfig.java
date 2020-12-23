package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class GenericConnectionAnalyzerSolverConfig {

	private GenericConnectionAnalyzerSolverConfig() {
		throw new IllegalAccessError();
	}

	private static final SystemParameters CONFIG = loadParameters("/configs/ConnectionAnalyzerSolverConfig.properties");

	/**
	 * The probability of the relation connection solver.
	 */
	public static final double RELATION_CONNECTION_SOLVER_PROBABILITY = CONFIG.getPropertyAsDouble("RelationConnectionSolver_Probability");

	/**
	 * The probability of the instance mapping connection solver.
	 */
	public static final double INSTANCE_CONNECTION_SOLVER_PROBABILITY = CONFIG.getPropertyAsDouble("InstanceConnectionSolver_Probability");
	/**
	 * The probability of the instance mapping connection solver, if the connection
	 * does not include the comparison of a type.
	 */
	public static final double INSTANCE_CONNECTION_SOLVER_PROBABILITY_WITHOUT_TYPE = CONFIG.getPropertyAsDouble("InstanceConnectionSolver_ProbabilityWithoutType");

	private static SystemParameters loadParameters(String filePath) {
		return new SystemParameters(filePath, true);
	}

}
