package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.IAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors.GenericRecommendationConfig;

/**
 * The Class RecommendationGenerator defines the recommendation stage.
 */
public class RecommendationGenerator implements IExecutionStage {

    private AgentDatastructure data;
    private List<IAgent> agents = new ArrayList<>();
    private RecommendationGeneratorConfig config;
    private GenericRecommendationConfig agentConfig;

    /**
     * Creates a new model connection agent with the given extraction state and ntr state.
     *
     * @param data the data
     */
    public RecommendationGenerator(AgentDatastructure data) {
        this(data, RecommendationGeneratorConfig.DEFAULT_CONFIG, GenericRecommendationConfig.DEFAULT_CONFIG);
    }

    /**
     * Instantiates a new recommendation generator.
     *
     * @param data        the data
     * @param config      the config
     * @param agentConfig the agent config
     */
    public RecommendationGenerator(AgentDatastructure data, RecommendationGeneratorConfig config, GenericRecommendationConfig agentConfig) {
        this.data = data;
        this.config = config;
        this.agentConfig = agentConfig;
        data.setRecommendationState(new RecommendationState());
        initializeAgents();
    }

    /**
     * Instantiates a new recommendation generator.
     */
    public RecommendationGenerator() {
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

        Map<String, RecommendationAgent> myAgents = Loader.loadLoadable(RecommendationAgent.class);

        for (String recommendationAgent : config.recommendationAgents) {
            if (!myAgents.containsKey(recommendationAgent)) {
                throw new IllegalArgumentException("RecommendationAgent " + recommendationAgent + " not found");
            }
            agents.add(myAgents.get(recommendationAgent).create(data, agentConfig));
        }

    }

    @Override
    public AgentDatastructure getBlackboard() {
        return data;
    }

    @Override
    public IExecutionStage create(AgentDatastructure data, Map<String, String> configs) {
        return new RecommendationGenerator(data, new RecommendationGeneratorConfig(configs), new GenericRecommendationConfig(configs));
    }
}
