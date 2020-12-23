package edu.kit.ipd.consistency_analyzer.modules;

public interface IAnalyzerSolverModule<S extends IState> extends IModule<S> {

	/**
	 * Runs finders. In contrast to analyzers finders aren't executed on a single
	 * node, but on the whole graph.
	 */
	void runSolvers();

	/**
	 * Runs the agents.
	 */
	void runAnalyzers();

}
