package edu.kit.kastel.mcse.ardoco.erid.api.diagramrecognitionmock;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

/**
 * This {@link PipelineStepData} gives access to the diagram goldstandard associated with the project to analyze.
 */
public record InputDiagramDataMock(DiagramProject diagramProject) implements PipelineStepData {
    public static final String ID = "InputDiagramDataMock";
}
