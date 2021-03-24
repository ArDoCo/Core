package edu.kit.ipd.consistency_analyzer.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.IAgent;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Loader;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.RecommendationAgent;
import edu.kit.ipd.consistency_analyzer.datastructures.RecommendationState;

public class RecommendationGenerator implements IAgentModule<AgentDatastructure> {

	private AgentDatastructure data;
	private List<RecommendationAgent> agents = new ArrayList<>();
	private RecommendationGeneratorConfig config;

	/**
	 * Creates a new model connection agent with the given extraction state and ntr state.
	 */
	public RecommendationGenerator(AgentDatastructure data) {
		this(data, RecommendationGeneratorConfig.DEFAULT_CONFIG);
	}

	public RecommendationGenerator(AgentDatastructure data, RecommendationGeneratorConfig config) {
		this.data = data;
		this.config = config;
		data.setRecommendationState(new RecommendationState());
		initializeAgents();
	}

	@Override
	public void exec() {
		runAgents();
	}

	/**
	 * Initializes graph dependent analyzers.
	 */
	private void initializeAgents() {

		Map<String, RecommendationAgent> myAgents = Loader.loadLoadable(RecommendationAgent.class);

		for (String recommendationAgent : config.recommendationAgents) {
			if (!myAgents.containsKey(recommendationAgent)) {
				throw new IllegalArgumentException("RecommendationAgent " + recommendationAgent + " not found");
			}
			agents.add(myAgents.get(recommendationAgent).create(data));
		}

	}

	/**
	 * Runs solvers, that create recommendations.
	 */
	@Override
	public void runAgents() {
		for (IAgent agent : agents) {
			agent.exec();
		}

	}

	@Override
	public AgentDatastructure getState() {
		return data;
	}

}
