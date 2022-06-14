/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.EnumMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

public class ConnectionStates implements IConnectionStates {
    private Map<Metamodel, ConnectionState> connectionStates;

    private ConnectionStates() {
        connectionStates = new EnumMap<>(Metamodel.class);
    }

    public static ConnectionStates build() {
        var recStates = new ConnectionStates();
        for (Metamodel mm : Metamodel.values()) {
            recStates.connectionStates.put(mm, new ConnectionState());
        }
        return recStates;
    }

    public ConnectionState getConnectionState(Metamodel mm) {
        return connectionStates.get(mm);
    }
}
