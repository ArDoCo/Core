/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Contains the {@link DiagramInconsistencyState} for each {@link Metamodel}.
 */
public interface DiagramInconsistencyStates extends PipelineStepData {
    String ID = "DiagramInconsistencyStates";

    /**
     * {@return the state for the given metamodel}
     *
     * @param mm the metamodel
     */
    DiagramInconsistencyState getDiagramInconsistencyState(Metamodel mm);
}
