/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface DiagramRecognitionState extends PipelineStepData {
    String ID = "DiagramRecognition";

    void addDiagram(Diagram diagram);

    List<Diagram> getDiagrams();
}
