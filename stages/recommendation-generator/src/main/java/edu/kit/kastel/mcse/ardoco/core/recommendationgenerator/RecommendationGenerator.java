/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents.InitialRecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents.PhraseRecommendationAgent;

/**
 * The Class RecommendationGenerator defines the recommendation stage.
 */
public class RecommendationGenerator extends AbstractExecutionStage {
    @Configurable
    private List<String> enabledAgents;

    /**
     * Creates a new model connection agent with the given extraction state and ntr state.
     */
    public RecommendationGenerator(DataRepository dataRepository) {
        super("RecommendationGenerator", dataRepository, List.of(//
                //new TermBuilder(dataRepository),//
                new InitialRecommendationAgent(dataRepository),//
                new PhraseRecommendationAgent(dataRepository)));

        this.enabledAgents = getAgents().stream().map(Agent::getId).toList();
    }

    /**
     * Creates a {@link RecommendationGenerator} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of {@link RecommendationGenerator}
     */
    public static RecommendationGenerator get(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var recommendationGenerator = new RecommendationGenerator(dataRepository);
        recommendationGenerator.applyConfiguration(additionalConfigs);
        return recommendationGenerator;
    }

    @Override
    protected void initializeState() {
        var recommendationStates = RecommendationStatesImpl.build();
        getDataRepository().addData(RecommendationStates.ID, recommendationStates);
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return findByClassName(enabledAgents, getAgents());
    }
}
