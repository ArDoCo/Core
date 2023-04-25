package edu.kit.kastel.mcse.ardoco.core.codetraceability;

import java.util.EnumMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.SamCodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.SamCodeTraceabilityStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;

public class SamCodeTraceabilityStatesImpl implements SamCodeTraceabilityStates {

    private Map<Metamodel, SamCodeTraceabilityStateImpl> samCodeTraceabilityStates;

    private SamCodeTraceabilityStatesImpl() {
        samCodeTraceabilityStates = new EnumMap<>(Metamodel.class);
    }

    public static SamCodeTraceabilityStatesImpl build() {
        var recStates = new SamCodeTraceabilityStatesImpl();
        for (Metamodel mm : Metamodel.values()) {
            recStates.samCodeTraceabilityStates.put(mm, new SamCodeTraceabilityStateImpl());
        }
        return recStates;
    }

    @Override
    public SamCodeTraceabilityState getSamCodeTraceabilityState(Metamodel mm) {
        return samCodeTraceabilityStates.get(mm);
    }
}
