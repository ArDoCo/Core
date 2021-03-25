package edu.kit.ipd.consistency_analyzer.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.ipd.consistency_analyzer.agents_extractors.GenericConnectionAnalyzerSolverConfig;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.ConnectionAgent;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.IAgent;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Loader;
import edu.kit.ipd.consistency_analyzer.datastructures.ConnectionState;

/**
 * The ModelConnectionAgent runs different analyzers and solvers. This agent creates recommendations as well as
 * matchings between text and model. The order is important: All connections should run after the recommendations have
 * been made.
 *
 * @author Sophie
 *
 */
public class ConnectionGenerator implements IAgentModule<AgentDatastructure> {

	private AgentDatastructure data;

	private List<IAgent> agents = new ArrayList<>();

	private ConnectionGeneratorConfig config;
	private GenericConnectionAnalyzerSolverConfig agentConfig;

	/**
	 * Creates a new model connection agent with the given extraction states.
	 */
	public ConnectionGenerator(AgentDatastructure data) {
		this(data, ConnectionGeneratorConfig.DEFAULT_CONFIG, GenericConnectionAnalyzerSolverConfig.DEFAULT_CONFIG);
	}

	/**
	 * Creates a new model connection agent with the given extraction states.
	 */
	public ConnectionGenerator(AgentDatastructure data, ConnectionGeneratorConfig config, GenericConnectionAnalyzerSolverConfig agentConfig) {
		this.data = data;
		this.config = config;
		this.agentConfig = agentConfig;
		data.setConnectionState(new ConnectionState());
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

		Map<String, ConnectionAgent> myAgents = Loader.loadLoadable(ConnectionAgent.class);

		for (String connectionAnalyzer : config.connectionAgents) {
			if (!myAgents.containsKey(connectionAnalyzer)) {
				throw new IllegalArgumentException("ConnectionAnalyzer " + connectionAnalyzer + " not found");
			}
			agents.add(myAgents.get(connectionAnalyzer).create(data, agentConfig));
		}

	}

	/**
	 * Runs solvers, that connect model extraction State and Recommendation State.
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
