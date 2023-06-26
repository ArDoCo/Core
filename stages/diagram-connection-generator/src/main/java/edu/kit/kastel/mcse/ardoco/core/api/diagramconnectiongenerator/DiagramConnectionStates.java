package edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface DiagramConnectionStates extends PipelineStepData {
    String ID = "DiagramConnectionStates";

    DiagramConnectionState getDiagramConnectionState(Metamodel mm);
}
