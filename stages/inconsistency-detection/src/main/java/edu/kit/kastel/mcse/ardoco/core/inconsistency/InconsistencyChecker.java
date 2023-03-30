/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.InitialInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.MissingModelElementInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.UndocumentedModelElementInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class InconsistencyChecker extends AbstractExecutionStage {

    private final MutableList<PipelineAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    public InconsistencyChecker(DataRepository dataRepository) {
        super("InconsistencyChecker", dataRepository);

        agents = Lists.mutable.of(new InitialInconsistencyAgent(dataRepository), new MissingModelElementInconsistencyAgent(dataRepository),
                new UndocumentedModelElementInconsistencyAgent(dataRepository));
        enabledAgents = agents.collect(Agent::getId);
    }

    @Override
    protected void initializeState() {
        var inconsistencyStates = InconsistencyStatesImpl.build();
        getDataRepository().addData(InconsistencyStates.ID, inconsistencyStates);
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
