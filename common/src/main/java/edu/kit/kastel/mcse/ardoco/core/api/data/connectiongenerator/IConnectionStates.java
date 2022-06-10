package edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator;

import edu.kit.kastel.informalin.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

public interface IConnectionStates extends PipelineStepData {
    static final String ID = "ConnectionStates";

    IConnectionState getConnectionState(Metamodel mm);
}
