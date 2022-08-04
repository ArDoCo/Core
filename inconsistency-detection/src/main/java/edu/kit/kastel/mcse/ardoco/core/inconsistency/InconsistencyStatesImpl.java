/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.EnumMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

public class InconsistencyStatesImpl implements InconsistencyStates {
    private Map<Metamodel, InconsistencyStateImpl> inconsistencyStates;

    private InconsistencyStatesImpl() {
        inconsistencyStates = new EnumMap<>(Metamodel.class);
    }

    public static InconsistencyStatesImpl build() {
        var recStates = new InconsistencyStatesImpl();
        for (Metamodel mm : Metamodel.values()) {
            recStates.inconsistencyStates.put(mm, new InconsistencyStateImpl());
        }
        return recStates;
    }

    @Override
    public InconsistencyState getInconsistencyState(Metamodel mm) {
        return inconsistencyStates.get(mm);
    }
}
