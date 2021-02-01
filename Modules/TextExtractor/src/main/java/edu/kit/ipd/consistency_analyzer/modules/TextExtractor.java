package edu.kit.ipd.consistency_analyzer.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.ipd.consistency_analyzer.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents.IAgent;
import edu.kit.ipd.consistency_analyzer.agents.Loader;
import edu.kit.ipd.consistency_analyzer.agents.TextAgent;
import edu.kit.ipd.consistency_analyzer.datastructures.TextExtractionState;

public class TextExtractor implements IAgentModule<AgentDatastructure> {

    private AgentDatastructure data;
    private List<TextAgent> agents = new ArrayList<>();

    /**
     * Creates a new model connection agent with the given extraction states.
     *
     * @param graph                the PARSE graph
     * @param modelExtractionState the model extraction state
     * @param textExtractionState  the text extraction state
     * @param recommendationState  the state with the recommendations
     */
    public TextExtractor(AgentDatastructure data) {
        this.data = data;
        data.setTextState(new TextExtractionState());
        initializeAgents();
    }

    @Override
    public void exec() {
        runAgents();
    }

    /**
     * Initializes graph dependent analyzers and solvers
     */

    private void initializeAgents() {

        Map<String, TextAgent> myAgents = Loader.loadLoadable(TextAgent.class);

        for (String agent : TextExtractorConfig.TEXT_AGENTS) {
            if (!myAgents.containsKey(agent)) {
                throw new IllegalArgumentException("TextAgent " + agent + " not found");
            }
            agents.add(myAgents.get(agent).create(data));
        }
    }

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
