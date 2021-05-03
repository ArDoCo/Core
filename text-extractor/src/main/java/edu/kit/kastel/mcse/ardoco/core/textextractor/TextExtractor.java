package edu.kit.kastel.mcse.ardoco.core.textextractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.TextStateWithClustering;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.IAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IAgentModule;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IModule;
import edu.kit.kastel.mcse.ardoco.core.textextractor.agents_extractors.GenericTextConfig;

public class TextExtractor implements IAgentModule<AgentDatastructure> {

    private AgentDatastructure data;
    private List<IAgent> agents = new ArrayList<>();
    private final TextExtractorConfig config;
    private final GenericTextConfig agentConfig;

    /**
     * Creates a new model connection agent with the given extraction states.
     *
     * @param graph                the PARSE graph
     * @param modelExtractionState the model extraction state
     * @param textExtractionState  the text extraction state
     * @param recommendationState  the state with the recommendations
     */
    public TextExtractor(AgentDatastructure data) {
        this(data, TextExtractorConfig.DEFAULT_CONFIG, GenericTextConfig.DEFAULT_CONFIG);
    }

    public TextExtractor() {
        config = null;
        agentConfig = null;

    }

    public TextExtractor(AgentDatastructure data, TextExtractorConfig config, GenericTextConfig agentConfig) {
        this.data = data;
        this.config = config;
        this.agentConfig = agentConfig;
        // data.setTextState(new TextExtractionState());
        data.setTextState(new TextStateWithClustering(config.similarityPercentage));
        initializeAgents();
    }

    @Override
    public IModule<AgentDatastructure> create(AgentDatastructure data, Map<String, String> configs) {

        return new TextExtractor(data, new TextExtractorConfig(configs), new GenericTextConfig(configs));
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

        for (String agent : config.textAgents) {
            if (!myAgents.containsKey(agent)) {
                throw new IllegalArgumentException("TextAgent " + agent + " not found");
            }
            agents.add(myAgents.get(agent).create(data, agentConfig));
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
