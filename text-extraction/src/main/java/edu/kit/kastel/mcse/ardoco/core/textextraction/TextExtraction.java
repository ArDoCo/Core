/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.textextraction.agents.InitialTextAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.agents.PhraseAgent;

/**
 * The Class TextExtractor.
 */
public class TextExtraction extends AbstractExecutionStage {

    private final MutableList<PipelineAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    /**
     * Instantiates a new text extractor.
     */
    public TextExtraction(DataRepository dataRepository) {
        super("TextExtraction", dataRepository);
        this.agents = Lists.mutable.of(//
                new InitialTextAgent(dataRepository),//
                new PhraseAgent(dataRepository));
        this.enabledAgents = agents.collect(Agent::getId);
    }

    @Override
    protected void initializeState() {
        var dataRepository = getDataRepository();
        var optionalTextState = dataRepository.getData(TextState.ID, TextStateImpl.class);
        if (optionalTextState.isEmpty()) {
            var textState = new TextStateImpl();
            dataRepository.addData(TextState.ID, textState);
        }
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return findByClassName(enabledAgents, agents);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        for (var agent : agents) {
            agent.applyConfiguration(additionalConfiguration);
        }
    }
}
