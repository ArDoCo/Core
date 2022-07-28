/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.diagramdetection;

import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.Box;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.DiagramDetectionState;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiagramDetectionStateImpl extends AbstractState implements DiagramDetectionState {
    private Map<String, String> diagrams = new HashMap<>();
    private Map<String, List<Box>> boxes = new HashMap<>();

    @Override
    public void registerDiagram(String diagramId, String path) {
        File file = new File(path);
        if (!file.exists())
            throw new IllegalArgumentException("Diagram File has to exist");
        this.diagrams.put(diagramId, file.getAbsolutePath());
    }

    @Override
    public List<String> getDiagramIds() {
        return new ArrayList<>(this.diagrams.keySet());
    }

    @Override
    public List<Box> detectedBoxes(String diagramId) {
        if (!this.boxes.containsKey(diagramId))
            return null;
        return new ArrayList<>(this.boxes.get(diagramId));
    }

    @Override
    public void addBox(String diagramId, Color dominatingColor, Map<Color, List<String>> mentionedWordsByColor) {
        if (!this.diagrams.containsKey(diagramId))
            throw new IllegalArgumentException("Diagram " + diagramId + " is not known by this DiagramDetectionState");
        this.boxes.computeIfAbsent(diagramId, k -> new ArrayList<>()).add(new edu.kit.kastel.mcse.ardoco.core.diagramdetection.Box(dominatingColor, mentionedWordsByColor));

    }

    @Override
    public DiagramDetectionState createCopy() {
        var copy = new DiagramDetectionStateImpl();
        copy.diagrams = new HashMap<>(this.diagrams);
        copy.boxes = this.boxes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> new ArrayList<>(kv.getValue())));
        return copy;
    }
}
