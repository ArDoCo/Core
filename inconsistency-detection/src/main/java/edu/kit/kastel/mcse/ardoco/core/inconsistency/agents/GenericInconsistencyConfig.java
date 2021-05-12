package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public class GenericInconsistencyConfig extends Configuration {

    public static final GenericInconsistencyConfig DEFAULT_CONFIG = new GenericInconsistencyConfig();

    public GenericInconsistencyConfig() {
        super();
        SystemParameters config = new SystemParameters("/configs/InconsistencyCheckerConfig.properties", true);
        // TODO
    }

    public GenericInconsistencyConfig(Map<String, String> configs) {
        super();
        // TODO
    }

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of();
    }

}
