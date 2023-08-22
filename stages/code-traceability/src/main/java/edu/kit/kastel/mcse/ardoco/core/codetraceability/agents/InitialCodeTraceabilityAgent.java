/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.agents;

import java.util.List;
import java.util.SortedMap;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.ArCoTLInformant;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class InitialCodeTraceabilityAgent extends PipelineAgent {
    private final MutableList<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    public InitialCodeTraceabilityAgent(DataRepository dataRepository) {
        super(InitialCodeTraceabilityAgent.class.getSimpleName(), dataRepository);

        informants = Lists.mutable.of(new ArCoTLInformant(dataRepository));
        enabledInformants = informants.collect(Informant::getId);
    }

    @Override
    protected void initializeState() {
        // empty
    }

    @Override
    protected List<String> getEnabledPipelineStepIds() {
        return enabledInformants;
    }

    @Override
    protected List<Informant> getAllPipelineSteps() {
        return informants;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        informants.forEach(filter -> filter.applyConfiguration(additionalConfiguration));
    }
}
