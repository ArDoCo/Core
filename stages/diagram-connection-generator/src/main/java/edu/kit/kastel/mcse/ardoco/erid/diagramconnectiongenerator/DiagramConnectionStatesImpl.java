/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator;

import java.util.EnumMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;

/**
 * @see DiagramConnectionStates
 */
public class DiagramConnectionStatesImpl extends AbstractState implements DiagramConnectionStates {
    private final Map<Metamodel, DiagramConnectionState> diagramConnectionStates = new EnumMap<>(Metamodel.class);

    public DiagramConnectionStatesImpl() {
        super();
        for (Metamodel mm : Metamodel.values()) {
            diagramConnectionStates.put(mm, new DiagramConnectionStateImpl());
        }
    }

    @Override
    public DiagramConnectionState getDiagramConnectionState(Metamodel mm) {
        return diagramConnectionStates.get(mm);
    }
}
