/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants.ExtractionDependentOccurrenceInformant;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants.NameTypeConnectionInformant;

/**
 * The agent that executes the extractors of this stage.
 */
public class InitialConnectionAgent extends PipelineAgent {
    private final List<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    /**
     * Create the agent.
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
