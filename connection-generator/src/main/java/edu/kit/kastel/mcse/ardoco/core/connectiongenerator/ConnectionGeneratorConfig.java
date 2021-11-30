/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;


import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.ResourceAccessor;
import java.util.Map;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * The configuration for the connection generator module
 *
 * @author Dominik Fuchss
 */
public class ConnectionGeneratorConfig extends Configuration {

    private static final String CONNECTION_AGENTS = "Connection_Agents";
    /**
     * The default configuration of the module.
     */
    public static final ConnectionGeneratorConfig DEFAULT_CONFIG = new ConnectionGeneratorConfig();

    private ConnectionGeneratorConfig() {
        var config = new ResourceAccessor("/configs/ConnectionGenerator.properties", true);
        connectionAgents = config.getPropertyAsList(CONNECTION_AGENTS);
    }

    /**
     * Create the configuration based on the default config values.
     *
     * @param configs contains the keys that have to be overwritten
     */
    public ConnectionGeneratorConfig(Map<String, String> configs) {
        connectionAgents = getPropertyAsList(CONNECTION_AGENTS, configs);
    }

    /**
     * The list of solver types that should work on the connection state.
     */
    public final ImmutableList<String> connectionAgents;

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(CONNECTION_AGENTS, String.join(" ", connectionAgents));
    }

}
