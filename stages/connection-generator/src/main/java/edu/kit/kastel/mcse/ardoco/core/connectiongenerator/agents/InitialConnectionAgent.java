/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants.ExtractionDependentOccurrenceInformant;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants.NameTypeConnectionInformant;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * The agent that executes the extractors of this stage.
 */
public class InitialConnectionAgent extends PipelineAgent {
    private final List<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    /**
     * Create the agent.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public InitialConnectionAgent(DataRepository dataRepository) {
        super(InitialConnectionAgent.class.getSimpleName(), dataRepository);

        informants = List.of(new NameTypeConnectionInformant(dataRepository), new ExtractionDependentOccurrenceInformant(dataRepository));
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
