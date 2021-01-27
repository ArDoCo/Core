package edu.kit.ipd.consistency_analyzer.agents;

public abstract class ResolverAgent extends Agent {

	protected ResolverAgent(DependencyType dependencyType) {
		super(dependencyType);
	}

	@Override
	public abstract void exec();

}
