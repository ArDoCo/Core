/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.agents.ArchitectureLinkToCodeLinkTransformerAgent;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class SadCodeTraceabilityLinkRecovery extends AbstractExecutionStage {

    private final MutableList<PipelineAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    public SadCodeTraceabilityLinkRecovery(DataRepository dataRepository) {
        super(SadCodeTraceabilityLinkRecovery.class.getSimpleName(), dataRepository);

        agents = Lists.mutable.of(new ArchitectureLinkToCodeLinkTransformerAgent(dataRepository));
        enabledAgents = agents.collect(Agent::getId);
    }

    public static SadCodeTraceabilityLinkRecovery get(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var sadSamCodeTraceabilityLinkRecovery = new SadCodeTraceabilityLinkRecovery(dataRepository);
        sadSamCodeTraceabilityLinkRecovery.applyConfiguration(additionalConfigs);
        return sadSamCodeTraceabilityLinkRecovery;
    }

    @Override
    protected void initializeState() {
        DataRepository dataRepository = getDataRepository();
        if (!DataRepositoryHelper.hasCodeTraceabilityState(dataRepository)) {
            var codeTraceabilityState = new CodeTraceabilityStateImpl();
            dataRepository.addData(CodeTraceabilityState.ID, codeTraceabilityState);
        }
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
