package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public class ConnectionGeneratorConfig extends Configuration {

    public static final ConnectionGeneratorConfig DEFAULT_CONFIG = new ConnectionGeneratorConfig();

    private ConnectionGeneratorConfig() {
        SystemParameters config = new SystemParameters("/configs/ConnectionGenerator.properties", true);
        connectionAgents = config.getPropertyAsList("Connection_Agents");
    }

    public ConnectionGeneratorConfig(Map<String, String> configs) {
        connectionAgents = getPropertyAsList("Connection_Agents", configs);
    }

    /**
     * The list of solver types that should work on the connection state.
     */
    public final List<String> connectionAgents;

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of("Connection_Agents", String.join(" ", connectionAgents));
    }

}
