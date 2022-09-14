/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.EnumMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

public class ConnectionStatesImpl implements ConnectionStates {
    private Map<Metamodel, ConnectionStateImpl> connectionStates;

    private ConnectionStatesImpl() {
        connectionStates = new EnumMap<>(Metamodel.class);
    }

    public static ConnectionStatesImpl build() {
        var recStates = new ConnectionStatesImpl();
        for (Metamodel mm : Metamodel.values()) {
            recStates.connectionStates.put(mm, new ConnectionStateImpl());
        }
        return recStates;
    }

    public ConnectionStateImpl getConnectionState(Metamodel mm) {
        return connectionStates.get(mm);
    }
}
