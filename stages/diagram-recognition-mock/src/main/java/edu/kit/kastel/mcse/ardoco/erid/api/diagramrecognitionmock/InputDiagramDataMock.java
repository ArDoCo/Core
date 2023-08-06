package edu.kit.kastel.mcse.ardoco.erid.api.diagramrecognitionmock;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

/**
 * This {@link PipelineStepData} gives access to the diagram goldstandard associated with the project to analyze.
 */
public class InputDiagramDataMock implements PipelineStepData {
    public static final String ID = "InputDiagramDataMock";

    private final DiagramProject diagramProject;

    public InputDiagramDataMock(DiagramProject diagramProject) {
        this.diagramProject = diagramProject;
    }

    public DiagramProject getDiagramProject() {
        return diagramProject;
    }
}
