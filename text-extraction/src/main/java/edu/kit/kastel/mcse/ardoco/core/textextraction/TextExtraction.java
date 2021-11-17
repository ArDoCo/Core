package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.IAgent;
import edu.kit.kastel.mcse.ardoco.core.common.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.common.Loader;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextAgent;

/**
 * The Class TextExtractor.
 */
public class TextExtraction implements IExecutionStage {

    private AgentDatastructure data;
    private MutableList<IAgent> agents = Lists.mutable.empty();
    private final TextExtractionConfig config;
    private final GenericTextConfig agentConfig;

    /**
     * Creates a new model connection agent with the given extraction states.
     *
     * @param data the data for the stage
     */
    public TextExtraction(AgentDatastructure data) {
        this(data, TextExtractionConfig.DEFAULT_CONFIG, GenericTextConfig.DEFAULT_CONFIG);
    }

    /**
     * Instantiates a new text extractor.
     */
    public TextExtraction() {
        config = null;
        agentConfig = null;

    }

    /**
     * Instantiates a new text extractor.
     *
     * @param data        the data
     * @param config      the config
     * @param agentConfig the agent config
     */
    public TextExtraction(AgentDatastructure data, TextExtractionConfig config, GenericTextConfig agentConfig) {
        this.data = data;
        this.config = config;
        this.agentConfig = agentConfig;
        data.setTextState(new TextState(config.similarityPercentage));
        initializeAgents();
    }

    @Override
    public IExecutionStage create(AgentDatastructure data, Map<String, String> configs) {
        return new TextExtraction(data, new TextExtractionConfig(configs), new GenericTextConfig(configs));
    }

    @Override
    public void exec() {
        for (IAgent agent : agents) {
            agent.exec();
        }
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
    public AgentDatastructure getBlackboard() {
        return data;
    }
}
