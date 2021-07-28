package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.ResourceAccessor;

public class InconsistencyConfig extends Configuration {

    public static final InconsistencyConfig DEFAULT_CONFIG = new InconsistencyConfig();

    private static final String MISSING_MODEL_ELEMENT_INCONSISTENCY_AGENT_THRESHOLD = "MissingModelElementInconsistencyAgent_threshold";

    private final double missingModelInstanceInconsistencyThreshold;

    public InconsistencyConfig() {
        super();
        var config = new ResourceAccessor("/configs/InconsistencyCheckerConfig.properties", true);

        missingModelInstanceInconsistencyThreshold = config.getPropertyAsDouble(MISSING_MODEL_ELEMENT_INCONSISTENCY_AGENT_THRESHOLD);
    }

    public InconsistencyConfig(Map<String, String> configs) {
        super();
        missingModelInstanceInconsistencyThreshold = Double.parseDouble(configs.get(MISSING_MODEL_ELEMENT_INCONSISTENCY_AGENT_THRESHOLD));
    }

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(MISSING_MODEL_ELEMENT_INCONSISTENCY_AGENT_THRESHOLD, Double.toString(missingModelInstanceInconsistencyThreshold));
    }

    public double getMissingModelInstanceInconsistencyThreshold() {
        return missingModelInstanceInconsistencyThreshold;
    }

}
