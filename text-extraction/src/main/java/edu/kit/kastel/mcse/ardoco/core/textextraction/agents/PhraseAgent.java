/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.informalin.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.PhraseAgentInformant;

/**
 * Agent that is responsible for looking at phrases and extracting {@link NounMapping}s from compound nouns etc.
 */
public class PhraseAgent extends PipelineAgent {

    private final List<Informant> extractors;

    @Configurable
    private List<String> enabledExtractors;

    /**
     * Instantiates a new initial text agent.
     */
    public PhraseAgent(DataRepository dataRepository) {
        super(PhraseAgent.class.getSimpleName(), dataRepository);
        extractors = List.of(new PhraseAgentInformant(dataRepository));
        enabledExtractors = extractors.stream().map(AbstractPipelineStep::getId).toList();
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
