/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency;

import edu.kit.kastel.informalin.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

public interface InconsistencyStates extends PipelineStepData {
    static final String ID = "InconsistencyStates";

    InconsistencyState getInconsistencyState(Metamodel mm);
}
