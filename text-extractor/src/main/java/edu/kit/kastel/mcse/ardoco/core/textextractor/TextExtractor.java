package edu.kit.kastel.mcse.ardoco.core.textextractor;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.TextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.IAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IExecutionStage;

/**
 * The Class TextExtractor.
 */
public class TextExtractor implements IExecutionStage {

    private AgentDatastructure data;
    private MutableList<IAgent> agents = Lists.mutable.empty();
    private final TextExtractorConfig config;
    private final GenericTextConfig agentConfig;

    /**
     * Creates a new model connection agent with the given extraction states.
     *
     * @param data the data for the stage
     */
    public TextExtractor(AgentDatastructure data) {
        this(data, TextExtractorConfig.DEFAULT_CONFIG, GenericTextConfig.DEFAULT_CONFIG);
    }

    /**
     * Instantiates a new text extractor.
     */
    public TextExtractor() {
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
    public TextExtractor(AgentDatastructure data, TextExtractorConfig config, GenericTextConfig agentConfig) {
        this.data = data;
        this.config = config;
        this.agentConfig = agentConfig;
        data.setTextState(new TextState(config.similarityPercentage));
        initializeAgents();
    }

    @Override
    public IExecutionStage create(AgentDatastructure data, Map<String, String> configs) {
        return new TextExtractor(data, new TextExtractorConfig(configs), new GenericTextConfig(configs));
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
