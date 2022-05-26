package edu.kit.kastel.mcse.ardoco.core.inconsistency.filters;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractFilter;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgentData;

public class TraceLinkFilter extends AbstractFilter<InconsistencyAgentData> {

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }

    @Override
    public void exec(InconsistencyAgentData data) {
        // do something here
    }
}
