/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents.InitialRecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents.PhraseRecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextState;

/**
 * The Class RecommendationGenerator defines the recommendation stage.
 */
public class RecommendationGenerator extends AbstractExecutionStage {

    private final MutableList<RecommendationAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    /**
     * Creates a new model connection agent with the given extraction state and ntr state.
     */
    public RecommendationGenerator(DataRepository dataRepository) {
        super("RecommendationGenerator", dataRepository);

        this.agents = Lists.mutable.of(new InitialRecommendationAgent(dataRepository), new PhraseRecommendationAgent(dataRepository));
        this.enabledAgents = agents.collect(IAgent::getId);
    }

    @Override
    public void run() {
        var recommendationStates = RecommendationStates.build();
        getDataRepository().addData(RecommendationStates.ID, recommendationStates);

        for (RecommendationAgent agent : findByClassName(enabledAgents, agents)) {
            this.addPipelineStep(agent);
        }
        super.run();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        for (var agent : agents) {
            agent.applyConfiguration(additionalConfiguration);
        }
    }

    public static IText getAnnotatedText(DataRepository dataRepository) {
        return dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow().getText();
    }

    public static ITextState getTextState(DataRepository dataRepository) {
        return dataRepository.getData(TextState.ID, TextState.class).orElseThrow();
    }

    public static ModelStates getModelStatesData(DataRepository dataRepository) {
        return dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow();
    }

    public static IRecommendationStates getRecommendationStates(DataRepository dataRepository) {
        return dataRepository.getData(IRecommendationStates.ID, IRecommendationStates.class).orElseThrow();
    }

}
