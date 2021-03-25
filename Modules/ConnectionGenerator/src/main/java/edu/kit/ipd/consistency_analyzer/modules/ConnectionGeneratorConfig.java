package edu.kit.ipd.consistency_analyzer.modules;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Configuration;
import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class ConnectionGeneratorConfig extends Configuration {

    public static final ConnectionGeneratorConfig DEFAULT_CONFIG = new ConnectionGeneratorConfig();

    private ConnectionGeneratorConfig() {
        SystemParameters config = new SystemParameters("/configs/ConnectionGenerator.properties", true);
        connectionAgents = config.getPropertyAsList("Connection_Agents");
    }

    public ConnectionGeneratorConfig(List<String> connectionAgents) {
        this.connectionAgents = connectionAgents;
    }

    /**
     * The list of solver types that should work on the connection state.
     */
    public final List<String> connectionAgents;

}
