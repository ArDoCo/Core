package edu.kit.kastel.mcse.ardoco.core.api.diagraminconsistency;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface DiagramInconsistencyStates extends PipelineStepData {
    public static final String ID = "DiagramInconsistencyStates";

    DiagramInconsistencyState getDiagramInconsistencyState(Metamodel mm);
}
