/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors.ExtractionDependentOccurrenceExtractor;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors.NameTypeConnectionExtractor;

/**
 * The agent that executes the extractors of this stage.
 */
public class InitialConnectionAgent extends PipelineAgent {
    private final List<Informant> extractors;

    @Configurable
    private List<String> enabledExtractors;

    /**
     * Create the agent.
     */
    public InitialConnectionAgent(DataRepository dataRepository) {
        super("InitialConnectionAgent", dataRepository);

        extractors = List.of(new NameTypeConnectionExtractor(dataRepository), new ExtractionDependentOccurrenceExtractor(dataRepository));
        enabledExtractors = extractors.stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledExtractors, extractors);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        extractors.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
