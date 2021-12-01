/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.ResourceAccessor;

public class GenericInconsistencyConfig extends Configuration {

    public static final GenericInconsistencyConfig DEFAULT_CONFIG = new GenericInconsistencyConfig();

    private static final String MISSING_MODEL_ELEMENT_INCONSISTENCY_AGENT_THRESHOLD = "MissingModelElementInconsistencyAgent_threshold";

    private final double missingModelInstanceInconsistencyThreshold;

    public GenericInconsistencyConfig() {
        super();
        var config = new ResourceAccessor("/configs/InconsistencyCheckerConfig.properties", true);

        missingModelInstanceInconsistencyThreshold = config.getPropertyAsDouble(MISSING_MODEL_ELEMENT_INCONSISTENCY_AGENT_THRESHOLD);
    }

    public GenericInconsistencyConfig(Map<String, String> configs) {
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
