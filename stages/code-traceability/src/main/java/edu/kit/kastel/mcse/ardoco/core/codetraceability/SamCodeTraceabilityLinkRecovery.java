/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.SamCodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.agents.InitialCodeTraceabilityAgent;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class SamCodeTraceabilityLinkRecovery extends AbstractExecutionStage {

    private final MutableList<PipelineAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    public SamCodeTraceabilityLinkRecovery(DataRepository dataRepository) {
        super(SamCodeTraceabilityLinkRecovery.class.getSimpleName(), dataRepository);

        agents = Lists.mutable.of(new InitialCodeTraceabilityAgent(dataRepository));
        enabledAgents = agents.collect(Agent::getId);
    }

    @Override
    protected void initializeState() {
        var samCodeTraceabilityState = new SamCodeTraceabilityStateImpl();
        getDataRepository().addData(SamCodeTraceabilityState.ID, samCodeTraceabilityState);
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return findByClassName(enabledAgents, agents);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        for (var agent : agents) {
            agent.applyConfiguration(additionalConfiguration);
        }
    }
}
