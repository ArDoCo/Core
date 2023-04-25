package edu.kit.kastel.mcse.ardoco.core.api.codetraceability;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface SamCodeTraceabilityStates extends PipelineStepData {
    String ID = "SamCodeTraceabilityStates";

    SamCodeTraceabilityState getSamCodeTraceabilityState(Metamodel mm);

}
