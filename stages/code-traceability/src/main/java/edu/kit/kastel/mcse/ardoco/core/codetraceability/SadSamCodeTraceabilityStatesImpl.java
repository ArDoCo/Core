package edu.kit.kastel.mcse.ardoco.core.codetraceability;

import java.util.EnumMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.SadSamCodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.SadSamCodeTraceabilityStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;

public class SadSamCodeTraceabilityStatesImpl implements SadSamCodeTraceabilityStates {

    private Map<Metamodel, SadSamCodeTraceabilityStateImpl> sadSamCodeTraceabilityStates;

    private SadSamCodeTraceabilityStatesImpl() {
        sadSamCodeTraceabilityStates = new EnumMap<>(Metamodel.class);
    }

    public static SadSamCodeTraceabilityStatesImpl build() {
        var recStates = new SadSamCodeTraceabilityStatesImpl();
        for (Metamodel mm : Metamodel.values()) {
            recStates.sadSamCodeTraceabilityStates.put(mm, new SadSamCodeTraceabilityStateImpl());
        }
        return recStates;
    }

    @Override
    public SadSamCodeTraceabilityState getSadSamCodeTraceabilityState(Metamodel mm) {
        return sadSamCodeTraceabilityStates.get(mm);
    }

}
