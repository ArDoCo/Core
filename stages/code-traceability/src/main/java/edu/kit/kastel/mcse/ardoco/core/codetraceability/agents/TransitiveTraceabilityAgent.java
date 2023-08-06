/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.TraceLinkCombiner;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class TransitiveTraceabilityAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants;

    public TransitiveTraceabilityAgent(DataRepository dataRepository) {
        super(TransitiveTraceabilityAgent.class.getSimpleName(), dataRepository, List.of(new TraceLinkCombiner(dataRepository)));
        enabledInformants = getInformantClassNames();
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
