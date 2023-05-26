/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.informants.CompoundAgentInformant;

/**
 * Agent that is responsible for looking at phrases and extracting {@link NounMapping}s from compound nouns etc.
 */
public class PhraseAgent extends PipelineAgent {

    private final List<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    /**
     * Instantiates a new initial text agent.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public PhraseAgent(DataRepository dataRepository) {
        super(PhraseAgent.class.getSimpleName(), dataRepository);
        informants = List.of(new CompoundAgentInformant(dataRepository));
        enabledInformants = informants.stream().map(AbstractPipelineStep::getId).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, informants);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        informants.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }

}
