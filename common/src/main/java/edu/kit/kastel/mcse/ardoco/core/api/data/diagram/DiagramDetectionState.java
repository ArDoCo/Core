/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.diagram;

import java.awt.*;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.PipelineStepData;
import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.informalin.framework.configuration.IConfigurable;

public interface DiagramDetectionState extends ICopyable<DiagramDetectionState>, IConfigurable, PipelineStepData {
    String ID = "DiagramDetectionState";

    void registerDiagram(String diagramId, String path);

    List<String> getDiagramIds();

    List<Box> detectedBoxes(String diagramId);

    void addBox(String diagramId, Color dominatingColor, Map<Color, List<String>> mentionedWordsByColor);
}
