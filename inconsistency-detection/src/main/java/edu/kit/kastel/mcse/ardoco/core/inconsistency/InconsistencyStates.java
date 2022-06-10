package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.EnumMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

public class InconsistencyStates implements IInconsistencyStates {
    private Map<Metamodel, InconsistencyState> inconsistencyStates;

    private InconsistencyStates() {
        inconsistencyStates = new EnumMap<>(Metamodel.class);
    }

    public static InconsistencyStates build() {
        var recStates = new InconsistencyStates();
        for (Metamodel mm : Metamodel.values()) {
            recStates.inconsistencyStates.put(mm, new InconsistencyState());
        }
        return recStates;
    }

    @Override
    public IInconsistencyState getInconsistencyState(Metamodel mm) {
        return inconsistencyStates.get(mm);
    }
}
