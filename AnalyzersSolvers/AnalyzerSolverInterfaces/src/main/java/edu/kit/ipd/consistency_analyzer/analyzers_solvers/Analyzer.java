package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

public abstract class Analyzer implements IAnalyzer {

	protected DependencyType type;

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
		return type;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}