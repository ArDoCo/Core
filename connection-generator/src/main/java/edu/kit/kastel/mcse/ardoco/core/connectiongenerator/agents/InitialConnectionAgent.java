/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors.ExtractionDependentOccurrenceExtractor;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors.NameTypeConnectionExtractor;

/**
 * The agent that executes the extractors of this stage.
 */
public class InitialConnectionAgent extends ConnectionAgent {
    private MutableList<AbstractExtractor<ConnectionAgentData>> extractors = Lists.mutable.of(new NameTypeConnectionExtractor(),
            new ExtractionDependentOccurrenceExtractor());

    @Configurable
    private List<String> enabledExtractors = extractors.collect(e -> e.getClass().getSimpleName());

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
