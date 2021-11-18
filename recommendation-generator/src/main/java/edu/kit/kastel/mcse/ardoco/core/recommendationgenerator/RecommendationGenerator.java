package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.IAgent;
import edu.kit.kastel.mcse.ardoco.core.common.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.common.Loader;

/**
 * The Class RecommendationGenerator defines the recommendation stage.
 */
public class RecommendationGenerator implements IExecutionStage {

    private AgentDatastructure data;
    private MutableList<IAgent> recommendationAgents = Lists.mutable.empty();
    private MutableList<IAgent> dependencyAgents = Lists.mutable.empty();

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
        for (IAgent agent : recommendationAgents) {
            agent.exec();
        }
        for (IAgent agent : dependencyAgents) {
            agent.exec();
        }
    }

    /**
     * Initializes graph dependent analyzers.
     */
    private void initializeAgents() {

        Map<String, RecommendationAgent> recommendationAgentsList = Loader.loadLoadable(RecommendationAgent.class);

        for (String recommendationAgent : config.recommendationAgents) {
            if (!recommendationAgentsList.containsKey(recommendationAgent)) {
                throw new IllegalArgumentException("RecommendationAgent " + recommendationAgent + " not found");
            }
            recommendationAgents.add(recommendationAgentsList.get(recommendationAgent).create(data, agentConfig));
        }

        Map<String, DependencyAgent> dependencyAgentsList = Loader.loadLoadable(DependencyAgent.class);
        for (String dependencyAgent : config.dependencyAgents) {
            if (!dependencyAgentsList.containsKey(dependencyAgent)) {
                throw new IllegalArgumentException("DependencyAgent " + dependencyAgent + " not found");
            }
            dependencyAgents.add(dependencyAgentsList.get(dependencyAgent).create(data, agentConfig));
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
