/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.ArCoTLInformant;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class InitialCodeTraceabilityAgent extends PipelineAgent {

    @Configurable
    private List<String> enabledInformants;

    public InitialCodeTraceabilityAgent(DataRepository dataRepository) {
        super(InitialCodeTraceabilityAgent.class.getSimpleName(), dataRepository, List.of(new ArCoTLInformant(dataRepository)));
        enabledInformants = getInformantIds();
    }

    @Override
    protected void initializeState() {
        // empty
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
