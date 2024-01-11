/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.EnumMap;

import edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

public class ConnectionStatesImpl implements ConnectionStates {
    private final EnumMap<Metamodel, ConnectionStateImpl> connectionStates;

    private ConnectionStatesImpl() {
        connectionStates = new EnumMap<>(Metamodel.class);
    }

    public static ConnectionStatesImpl build(DataRepository dataRepository) {
        var recStates = new ConnectionStatesImpl();
        for (Metamodel mm : Metamodel.values()) {
            recStates.connectionStates.put(mm, new ConnectionStateImpl(dataRepository));
        }
        return recStates;
    }

    @Override
    public ConnectionStateImpl getConnectionState(Metamodel mm) {
        return connectionStates.get(mm);
    }
}
