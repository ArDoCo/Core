/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants.ReferenceInformant;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * The reference solver finds instances mentioned in the text extraction state as names. If it founds some similar names
 * it creates recommendations.
 */
public class ReferenceAgent extends PipelineAgent {

    private final List<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    /**
     * Create the agent.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public ReferenceAgent(DataRepository dataRepository) {
        super(ReferenceAgent.class.getSimpleName(), dataRepository);

        informants = List.of(new ReferenceInformant(dataRepository));
        enabledInformants = informants.stream().map(e -> e.getClass().getSimpleName()).toList();
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
