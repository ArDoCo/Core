/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractFilter;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.filters.RecommendedInstanceProbabilityFilter;

public class InitialInconsistencyAgent extends InconsistencyAgent {
    private final List<AbstractFilter<InconsistencyAgentData>> filters = List.of(new RecommendedInstanceProbabilityFilter());

    @Configurable
    private List<String> enabledFilters = filters.stream().map(e -> e.getClass().getSimpleName()).toList();

    public InitialInconsistencyAgent() {
        // empty
    }

    @Override
    public void execute(InconsistencyAgentData data) {
        for (var filter : findByClassName(enabledFilters, filters)) {
            filter.exec(data);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }
}
