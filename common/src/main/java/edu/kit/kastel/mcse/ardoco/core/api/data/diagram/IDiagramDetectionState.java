/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.diagram;

import java.util.List;

import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.informalin.framework.configuration.IConfigurable;

public interface IDiagramDetectionState extends ICopyable<IDiagramDetectionState>, IConfigurable {
    void registerDiagram(String diagramId, String path);

    List<String> getDiagramIds();

    List<IBox> detectedBoxes(String diagramId);

    void addBox(String diagramId, List<String> mentionedWords);
}
