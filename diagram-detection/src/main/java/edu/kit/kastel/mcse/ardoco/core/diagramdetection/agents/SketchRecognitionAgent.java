/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.diagramdetection.agents;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.kit.kastel.informalin.framework.common.tuple.Pair;
import edu.kit.kastel.lissa.swa.documentation.SketchRecognitionResult;
import edu.kit.kastel.lissa.swa.documentation.SketchRecognitionService;
import edu.kit.kastel.lissa.swa.documentation.TextBox;
import edu.kit.kastel.mcse.ardoco.core.api.agent.DiagramDetectionAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.DiagramDetectionData;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.IDiagramDetectionState;

public class SketchRecognitionAgent extends DiagramDetectionAgent {
    private static final List<String> SUPPORTED_FILES = List.of(".jpg", ".png", ".jpeg");
    private final SketchRecognitionService sketchRecognitionService = new SketchRecognitionService();

    @Override
    public void execute(DiagramDetectionData data) {
        if (data.getDiagramDirectory() == null)
            return;

        var images = data.getDiagramDirectory().list((file, name) -> isValid(name));
        if (images == null || images.length == 0) {
            logger.warn("Found no images in directory: {}", data.getDiagramDirectory());
            return;
        }
        for (int i = 0; i < images.length; i++)
            images[i] = Path.of(data.getDiagramDirectory().getAbsolutePath(), images[i]).toString();

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

        transformData(results, data.getDiagramDetectionState());
    }

    private void transformData(List<Pair<String, SketchRecognitionResult>> results, IDiagramDetectionState diagramDetectionState) {
        for (var diagramResult : results) {
            var id = UUID.nameUUIDFromBytes(diagramResult.first().getBytes(StandardCharsets.UTF_8)).toString();
            diagramDetectionState.registerDiagram(id, diagramResult.first());
            for (var box : diagramResult.second().getBoxes()) {
                var texts = box.getTexts().stream().map(TextBox::getText).toList();
                if (!texts.isEmpty())
                    diagramDetectionState.addBox(id, texts);
            }
        }
    }

    private boolean isValid(String name) {
        return SUPPORTED_FILES.stream().anyMatch(t -> name.toLowerCase().endsWith(t));
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {

    }
}
