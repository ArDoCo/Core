package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

public abstract class Analyzer implements IAnalyzer {

	protected DependencyType type;

	/**
	 * An analyzer is executed on the current node.
	 *
	 * @param node the current node
	 */
	@Override
	public abstract void exec(IWord node);

	/**
	 * Creates a new analyzer of the specified type.
	 *
	 * @param type the analyzer type
	 */
	protected Analyzer(DependencyType type) {
		this.type = type;
	}

	/**
	 * Returns the dependency type of the current analyzer.
	 *
	 * @return the dependency type of the current analyzer
	 */
	public DependencyType getDependencyType() {
		return this.type;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}