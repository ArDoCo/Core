/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.InDepArcsExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.NounExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.OutDepArcsExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.SeparatedNamesExtractor;

/**
 * The Class InitialTextAgent defines the agent that executes the extractors for the text stage.
 */
public class InitialTextAgent extends TextAgent {

    private final List<AbstractExtractor> extractors;

    @Configurable
    private List<String> enabledExtractors;

    /**
     * Instantiates a new initial text agent.
     */
    public InitialTextAgent(DataRepository data) {
        super("InitialTextAgent", data);
        extractors = List.of(new NounExtractor(data), new InDepArcsExtractor(data), new OutDepArcsExtractor(data), new SeparatedNamesExtractor(data));
        enabledExtractors = extractors.stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    public void run() {
        for (var extractor : findByClassName(enabledExtractors, extractors)) {
            this.addPipelineStep(extractor);
        }
        super.run();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        this.applyConfiguration(additionalConfiguration);
        extractors.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }

}
