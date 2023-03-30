/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

public interface ConnectionStates extends PipelineStepData {
    String ID = "ConnectionStates";

    ConnectionState getConnectionState(Metamodel mm);
}
