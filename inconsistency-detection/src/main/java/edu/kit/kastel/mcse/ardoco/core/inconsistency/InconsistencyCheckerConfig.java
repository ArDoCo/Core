package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.ResourceAccessor;

public class InconsistencyCheckerConfig extends Configuration {

    private static final String INCONSISTENCY_AGENTS_PROPERTY = "Inconsistency_Agents";

    /**
     * The list of analyzer types that should work on the recommendation state.
     */
    public final ImmutableList<String> inconsistencyAgents;

    public static final InconsistencyCheckerConfig DEFAULT_CONFIG = new InconsistencyCheckerConfig();

    private InconsistencyCheckerConfig() {
        var config = new ResourceAccessor("/configs/InconsistencyChecker.properties", true);
        inconsistencyAgents = config.getPropertyAsList(INCONSISTENCY_AGENTS_PROPERTY);
    }

    public InconsistencyCheckerConfig(Map<String, String> configs) {
        inconsistencyAgents = getPropertyAsList(INCONSISTENCY_AGENTS_PROPERTY, configs);
    }

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(INCONSISTENCY_AGENTS_PROPERTY, String.join(" ", inconsistencyAgents));
    }

}
