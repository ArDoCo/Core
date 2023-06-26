package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator;

import java.util.EnumMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;

public class DiagramConnectionStatesImpl implements DiagramConnectionStates {
    private final Map<Metamodel, DiagramConnectionState> diagramConnectionStates = new EnumMap<>(Metamodel.class);

    public DiagramConnectionStatesImpl() {
        for (Metamodel mm : Metamodel.values()) {
            diagramConnectionStates.put(mm, new DiagramConnectionStateImpl());
        }
    }

    @Override
    public DiagramConnectionState getDiagramConnectionState(Metamodel mm) {
        return diagramConnectionStates.get(mm);
    }
}
