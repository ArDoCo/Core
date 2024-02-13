/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Contains the {@link DiagramConnectionState} for each {@link Metamodel}.
 */
public interface DiagramConnectionStates extends PipelineStepData {
    String ID = "DiagramConnectionStates";

    /**
     * {@return the state for the specified metamodel}
     * 
     * @param mm the metamodel
     */
    DiagramConnectionState getDiagramConnectionState(Metamodel mm);
}
