/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.agents;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.TraceLinkCombiner;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class TransitiveTraceabilityAgent extends PipelineAgent {

    private final MutableList<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    public TransitiveTraceabilityAgent(DataRepository dataRepository) {
        super(TransitiveTraceabilityAgent.class.getSimpleName(), dataRepository);

        informants = Lists.mutable.of(new TraceLinkCombiner(dataRepository));
        enabledInformants = informants.collect(Informant::getId);
    }

    @Override
    protected void initializeState() {
        // empty
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, informants);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        informants.forEach(filter -> filter.applyConfiguration(additionalConfiguration));
    }
}
