package edu.kit.ipd.consistency_analyzer.agents;

public abstract class ResolverAgent extends Agent {

	protected ResolverAgent(DependencyType dependencyType) {
		super(dependencyType);
	}

	public abstract void exec();

}
