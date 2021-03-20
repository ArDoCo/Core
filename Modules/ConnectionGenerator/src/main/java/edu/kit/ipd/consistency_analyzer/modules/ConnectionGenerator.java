package edu.kit.ipd.consistency_analyzer.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.ConnectionAgent;
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

    private List<ConnectionAgent> agents = new ArrayList<>();

    /**
     * Creates a new model connection agent with the given extraction states.
     *
     * @param text                the PARSE graph
     * @param modelState          the model extraction state
     * @param textState           the text extraction state
     * @param recommendationState the state with the recommendations
     */
    public ConnectionGenerator(AgentDatastructure data) {
        this.data = data;
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

        for (String connectionAnalyzer : ConnectionGeneratorConfig.CONNECTION_AGENTS) {
            if (!myAgents.containsKey(connectionAnalyzer)) {
                throw new IllegalArgumentException("ConnectionAnalyzer " + connectionAnalyzer + " not found");
            }
            agents.add(myAgents.get(connectionAnalyzer).create(data));
        }

    }

    /**
     * Runs solvers, that connect model extraction State and Recommendation State.
     */
    @Override
    public void runAgents() {
        for (ConnectionAgent agent : agents) {
            agent.exec();
        }
    }

    @Override
    public AgentDatastructure getState() {
        return data;
    }
}
