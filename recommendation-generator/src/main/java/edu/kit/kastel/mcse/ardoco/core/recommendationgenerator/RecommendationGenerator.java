package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.*;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IAgentModule;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IModule;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors.GenericRecommendationConfig;

public class RecommendationGenerator implements IAgentModule<AgentDatastructure> {

    private AgentDatastructure data;
    private List<IAgent> recommendationAgents = new ArrayList<>();
    private List<IAgent> dependencyAgents = new ArrayList<>();
    private RecommendationGeneratorConfig config;
    private GenericRecommendationConfig agentConfig;

    /**
     * Creates a new model connection agent with the given extraction state and ntr state.
     */
    public RecommendationGenerator(AgentDatastructure data) {
        this(data, RecommendationGeneratorConfig.DEFAULT_CONFIG, GenericRecommendationConfig.DEFAULT_CONFIG);
    }

    public RecommendationGenerator(AgentDatastructure data, RecommendationGeneratorConfig config, GenericRecommendationConfig agentConfig) {
        this.data = data;
        this.config = config;
        this.agentConfig = agentConfig;
        data.setRecommendationState(new RecommendationState());
        initializeAgents();
    }

    public RecommendationGenerator() {
    }

    @Override
    public void exec() {
        runAgents();
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

    /**
     * Runs solvers, that create recommendations.
     */
    @Override
    public void runAgents() {
        for (IAgent agent : recommendationAgents) {
            agent.exec();
        }
        for (IAgent agent : dependencyAgents) {
            agent.exec();
        }

    }

    @Override
    public AgentDatastructure getState() {
        return data;
    }

    @Override
    public IModule<AgentDatastructure> create(AgentDatastructure data, Map<String, String> configs) {
        return new RecommendationGenerator(data, new RecommendationGeneratorConfig(configs), new GenericRecommendationConfig(configs));
    }
}
