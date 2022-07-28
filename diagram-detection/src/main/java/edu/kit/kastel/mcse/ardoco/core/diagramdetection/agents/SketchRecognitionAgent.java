/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.diagramdetection.agents;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.common.tuple.Pair;
import edu.kit.kastel.lissa.swa.api.sketches.SketchRecognitionResult;
import edu.kit.kastel.lissa.swa.documentation.SketchRecognitionService;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.DiagramDetectionState;

public class SketchRecognitionAgent extends PipelineAgent {
    private static final List<String> SUPPORTED_FILES = List.of(".jpg", ".png", ".jpeg");
    private final SketchRecognitionService sketchRecognitionService = new SketchRecognitionService();
    private final File diagramDirectory;

    public SketchRecognitionAgent(DataRepository dataRepository, File diagramDirectory) {
        super(SketchRecognitionAgent.class.getSimpleName(), dataRepository);
        this.diagramDirectory = diagramDirectory;
    }

    @Override
    public void run() {
        if (diagramDirectory == null || !diagramDirectory.exists())
            return;

        var images = diagramDirectory.list((file, name) -> isValid(name));
        if (images == null || images.length == 0) {
            logger.warn("Found no images in directory: {}", diagramDirectory);
            return;
        }
        for (int i = 0; i < images.length; i++)
            images[i] = Path.of(diagramDirectory.getAbsolutePath(), images[i]).toString();

        sketchRecognitionService.start();
        List<Pair<String, SketchRecognitionResult>> results = new ArrayList<>();
        for (var image : images) {
            try {
                var result = sketchRecognitionService.recognize(new FileInputStream(image));
                logger.debug("Got result for {}", image);
                results.add(new Pair<>(image, result));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        sketchRecognitionService.stop();

        transformData(results, getDataRepository().getData(DiagramDetectionState.ID, DiagramDetectionState.class).orElseThrow());
    }

    private void transformData(List<Pair<String, SketchRecognitionResult>> results, DiagramDetectionState diagramDetectionState) {
        for (var diagramResult : results) {
            var id = UUID.nameUUIDFromBytes(diagramResult.first().getBytes(StandardCharsets.UTF_8)).toString();
            diagramDetectionState.registerDiagram(id, diagramResult.first());
            for (var box : diagramResult.second().getBoxes()) {
                Map<Color, List<String>> textsByColor = new LinkedHashMap<>();
                for (var text : box.getTexts()) {
                    if (!textsByColor.containsKey(toColor(text.getDominatingColor())))
                        textsByColor.put(toColor(text.getDominatingColor()), new ArrayList<>());
                    textsByColor.get(toColor(text.getDominatingColor())).add(text.getText());
                }
                diagramDetectionState.addBox(id, toColor(box.getDominatingColor()), textsByColor);
            }
        }
    }

    private Color toColor(Integer color) {
        return color == null ? null : new Color(color, true);
    }

    private boolean isValid(String name) {
        return SUPPORTED_FILES.stream().anyMatch(t -> name.toLowerCase().endsWith(t));
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // NOP
    }
}
