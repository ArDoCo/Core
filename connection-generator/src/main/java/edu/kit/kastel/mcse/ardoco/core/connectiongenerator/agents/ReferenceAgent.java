/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors.ReferenceExtractor;

/**
 * The reference solver finds instances mentioned in the text extraction state as names. If it founds some similar names
 * it creates recommendations.
 *
 */
public class ReferenceAgent extends PipelineAgent {

    private final List<Informant> extractors;

    @Configurable
    private List<String> enabledExtractors;

    /**
     * Create the agent.
     */
    public ReferenceAgent(DataRepository dataRepository) {
        super(ReferenceAgent.class.getSimpleName(), dataRepository);

        extractors = List.of(new ReferenceExtractor(dataRepository));
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
