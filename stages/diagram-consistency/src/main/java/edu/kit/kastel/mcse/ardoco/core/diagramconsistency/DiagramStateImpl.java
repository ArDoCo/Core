/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;

/**
 * Implementation of {@link DiagramState}.
 */
public class DiagramStateImpl implements DiagramState {

    private transient Diagram diagram = null;

    @Override
    public Diagram getDiagram() {
        return this.diagram;
    }

    @Override
    public void setDiagram(Diagram diagram) {
        this.diagram = diagram;
    }
}
