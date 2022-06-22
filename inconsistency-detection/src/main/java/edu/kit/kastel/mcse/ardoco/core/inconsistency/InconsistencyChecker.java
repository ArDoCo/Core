/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.InitialInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.MissingModelElementInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.MissingTextForModelElementInconsistencyAgent;

public class InconsistencyChecker extends AbstractExecutionStage {

    private final MutableList<InconsistencyAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    public InconsistencyChecker(DataRepository dataRepository) {
        super("InconsistencyChecker", dataRepository);

        agents = Lists.mutable.of(new InitialInconsistencyAgent(dataRepository), new MissingModelElementInconsistencyAgent(dataRepository),
                new MissingTextForModelElementInconsistencyAgent(dataRepository));
        enabledAgents = agents.collect(Agent::getId);
    }

    @Override
    public void run() {
        var inconsistencyStates = InconsistencyStatesImpl.build();
        getDataRepository().addData(InconsistencyStates.ID, inconsistencyStates);

        for (InconsistencyAgent agent : findByClassName(enabledAgents, agents)) {
            this.addPipelineStep(agent);
        }

        super.run();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        for (var agent : agents) {
            agent.applyConfiguration(additionalConfiguration);
        }
    }

}
