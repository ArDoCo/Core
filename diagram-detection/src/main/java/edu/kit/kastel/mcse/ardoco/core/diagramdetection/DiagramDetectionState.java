/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.diagramdetection;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.IBox;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.IDiagramDetectionState;

public class DiagramDetectionState extends AbstractState implements IDiagramDetectionState {
    private Map<String, String> diagrams = new HashMap<>();
    private Map<String, List<Box>> boxes = new HashMap<>();

    public DiagramDetectionState(Map<String, String> config) {
        super(config);
    }

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
    public List<IBox> detectedBoxes(String diagramId) {
        if (!this.boxes.containsKey(diagramId))
            return null;
        return new ArrayList<>(this.boxes.get(diagramId));
    }

    @Override
    public void addBox(String diagramId, Color dominatingColor, Map<Color, List<String>> mentionedWordsByColor) {
        if (!this.diagrams.containsKey(diagramId))
            throw new IllegalArgumentException("Diagram " + diagramId + " is not known by this DiagramDetectionState");
        this.boxes.computeIfAbsent(diagramId, k -> new ArrayList<>()).add(new Box(dominatingColor, mentionedWordsByColor));

    }

    @Override
    public IDiagramDetectionState createCopy() {
        var copy = new DiagramDetectionState(this.configs);
        copy.diagrams = new HashMap<>(this.diagrams);
        copy.boxes = this.boxes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> new ArrayList<>(kv.getValue())));
        return copy;
    }
}
