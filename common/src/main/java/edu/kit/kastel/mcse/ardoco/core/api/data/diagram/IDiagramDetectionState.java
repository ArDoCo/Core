/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.diagram;

import java.awt.*;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.informalin.framework.configuration.IConfigurable;

public interface IDiagramDetectionState extends ICopyable<IDiagramDetectionState>, IConfigurable {
    void registerDiagram(String diagramId, String path);

    List<String> getDiagramIds();

    List<IBox> detectedBoxes(String diagramId);

    void addBox(String diagramId, Color dominatingColor, Map<Color, List<String>> mentionedWordsByColor);
}
