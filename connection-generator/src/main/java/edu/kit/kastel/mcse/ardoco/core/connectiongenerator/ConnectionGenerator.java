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
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IExecutionStage;

/**
 * The ModelConnectionAgent runs different analyzers and solvers. This agent creates recommendations as well as
 * matchings between text and model. The order is important: All connections should run after the recommendations have
 * been made.
 *
 * @author Sophie
 *
 */
public class ConnectionGenerator implements IExecutionStage {

    private AgentDatastructure data;

    private List<IAgent> agents = new ArrayList<>();

    private ConnectionGeneratorConfig config;
    private GenericConnectionConfig agentConfig;

    /**
     * Create the module.
     */
    public ConnectionGenerator() {
    }

    /**
     * Creates a new model connection agent with the given extraction states.
     *
     * @param data the blackboard with all the data
     */
    public ConnectionGenerator(AgentDatastructure data) {
        this(data, ConnectionGeneratorConfig.DEFAULT_CONFIG, GenericConnectionConfig.DEFAULT_CONFIG);
    }

    /**
     * Creates a new model connection agent with the given extraction states.
     *
     * @param data        the blackboard with all the data
     * @param config      the configuration of the module
     * @param agentConfig the configuration of the agents
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
        for (IAgent agent : agents) {
            agent.exec();
        }
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

    @Override
    public AgentDatastructure getBlackboard() {
        return data;
    }

    @Override
    public IExecutionStage create(AgentDatastructure data, Map<String, String> configs) {
        return new ConnectionGenerator(data, new ConnectionGeneratorConfig(configs), new GenericConnectionConfig(configs));
    }
}
