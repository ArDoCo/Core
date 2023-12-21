/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Contains the loaded diagram.
 */
public interface DiagramState extends PipelineStepData {
    /**
     * The ID in the data repository.
     */
    String ID = "DiagramStateData";

    /**
     * Returns the diagram.
     *
     * @return The diagram. May be null.
     */
    Diagram getDiagram();

    /**
     * Sets the diagram. Overwrites the old diagram.
     *
     * @param diagram
     *                The diagram.
     */
    void setDiagram(Diagram diagram);
}
