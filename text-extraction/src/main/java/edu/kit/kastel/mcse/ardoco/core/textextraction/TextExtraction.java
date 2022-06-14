/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.textextraction.agents.ComputerScienceWordsAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.agents.InitialTextAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.agents.PhraseAgent;

/**
 * The Class TextExtractor.
 */
public class TextExtraction extends AbstractExecutionStage {

    private final MutableList<TextAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    /**
     * Instantiates a new text extractor.
     */
    public TextExtraction(DataRepository dataRepository) {
        super("TextExtraction", dataRepository);

        this.agents = Lists.mutable.of(new InitialTextAgent(dataRepository), new PhraseAgent(dataRepository), new ComputerScienceWordsAgent(dataRepository));
        this.enabledAgents = agents.collect(IAgent::getId);
    }

    @Override
    public void run() {
        initializeTextState();

        for (TextAgent agent : findByClassName(enabledAgents, agents)) {
            this.addPipelineStep(agent);
        }
        super.run();
    }

    private void initializeTextState() {
        var dataRepository = getDataRepository();
        var optionalTextState = dataRepository.getData(TextState.ID, TextState.class);
        if (optionalTextState.isEmpty()) {
            var textState = new TextState();
            dataRepository.addData(ITextState.ID, textState);
        }
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

    public static TextState getTextState(DataRepository dataRepository) {
        return dataRepository.getData(TextState.ID, TextState.class).orElseThrow();
    }

}
