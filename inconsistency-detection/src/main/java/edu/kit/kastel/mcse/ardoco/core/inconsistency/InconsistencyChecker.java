/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.InitialInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.MissingModelElementInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.MissingTextForModelElementInconsistencyAgent;

public class InconsistencyChecker extends AbstractExecutionStage {

    private MutableList<InconsistencyAgent> agents = Lists.mutable.of(new InitialInconsistencyAgent(), new MissingModelElementInconsistencyAgent(),
            new MissingTextForModelElementInconsistencyAgent());

    @Configurable
    private List<String> enabledAgents = agents.collect(IAgent::getId);

    public InconsistencyChecker() {
    }

    @Override
    public void execute(DataStructure data, Map<String, String> additionalSettings) {
        // Init new connection states
        data.getModelIds().forEach(mid -> data.setInconsistencyState(mid, new InconsistencyState()));

        this.applyConfiguration(additionalSettings);
        for (InconsistencyAgent agent : findByClassName(enabledAgents, agents)) {
            agent.applyConfiguration(additionalSettings);
            agent.execute(data);
        }
    }

}
