package edu.kit.kastel.mcse.ardoco.core.api.codetraceability;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface SadSamCodeTraceabilityStates extends PipelineStepData {
    String ID = "SadSamCodeTraceabilityStates";

    SadSamCodeTraceabilityState getSadSamCodeTraceabilityState(Metamodel mm);
}
