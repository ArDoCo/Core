/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors.ExtractionDependentOccurrenceExtractor;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors.NameTypeConnectionExtractor;

/**
 * The agent that executes the extractors of this stage.
 */
public class InitialConnectionAgent extends ConnectionAgent {
    private final List<AbstractExtractor<ConnectionAgentData>> extractors = List.of(new NameTypeConnectionExtractor(),
            new ExtractionDependentOccurrenceExtractor());

    @Configurable
    private List<String> enabledExtractors = extractors.stream().map(e -> e.getClass().getSimpleName()).toList();

    /**
     * Create the agent.
     */
    public InitialConnectionAgent() {
    }

    @Override
    public void execute(ConnectionAgentData data) {
        var text = data.getText();
        for (var extractor : findByClassName(enabledExtractors, extractors)) {
            for (IWord word : text.getWords()) {
                extractor.exec(data, word);
            }
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        extractors.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
