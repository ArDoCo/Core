package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency;

import java.util.HashMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyState;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;

/**
 * @see DiagramInconsistencyStates
 */
public class DiagramInconsistencyStatesImpl implements DiagramInconsistencyStates {
    private final Map<Metamodel, DiagramInconsistencyStateImpl> diagramInconsistencyStates = new HashMap<>();

    public DiagramInconsistencyStatesImpl() {
        for (Metamodel mm : Metamodel.values()) {
            diagramInconsistencyStates.put(mm, new DiagramInconsistencyStateImpl());
        }
    }

    @Override
    public DiagramInconsistencyState getDiagramInconsistencyState(Metamodel mm) {
        return diagramInconsistencyStates.get(mm);
    }
}
