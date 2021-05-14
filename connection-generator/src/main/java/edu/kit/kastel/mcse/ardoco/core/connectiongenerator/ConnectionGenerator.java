package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents_extractors.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.datastructures.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.IAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IAgentModule;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IModule;

/**
 * The ModelConnectionAgent runs different analyzers and solvers. This agent
 * creates recommendations as well as matchings between text and model. The
 * order is important: All connections should run after the recommendations have
 * been made.
 *
 * @author Sophie
 *
 */
public class ConnectionGenerator implements IAgentModule<AgentDatastructure> {

	private AgentDatastructure data;

	private List<IAgent> agents = new ArrayList<>();

	private ConnectionGeneratorConfig config;
	private GenericConnectionConfig agentConfig;

	public ConnectionGenerator() {
	}

	/**
	 * Creates a new model connection agent with the given extraction states.
	 */
	public ConnectionGenerator(AgentDatastructure data) {
		this(data, ConnectionGeneratorConfig.DEFAULT_CONFIG, GenericConnectionConfig.DEFAULT_CONFIG);
	}

	/**
	 * Creates a new model connection agent with the given extraction states.
	 */
	public ConnectionGenerator(AgentDatastructure data, ConnectionGeneratorConfig config, GenericConnectionConfig agentConfig) {
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

	@Override
	public IModule<AgentDatastructure> create(AgentDatastructure data, Map<String, String> configs) {
		return new ConnectionGenerator(data, new ConnectionGeneratorConfig(configs), new GenericConnectionConfig(configs));
	}
}
