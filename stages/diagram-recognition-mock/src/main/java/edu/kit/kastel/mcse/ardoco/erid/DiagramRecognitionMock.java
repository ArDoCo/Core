package edu.kit.kastel.mcse.ardoco.erid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramDataMock;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognitionStateImpl;

public class DiagramRecognitionMock extends AbstractExecutionStage {
    private static final ObjectMapper mapper = new ObjectMapper();

    public DiagramRecognitionMock(DataRepository dataRepository) {
        super("DiagramRecognitionMock", dataRepository);
    }

    public static DiagramRecognitionMock get(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var diagramRecognitionMock = new DiagramRecognitionMock(dataRepository);
        diagramRecognitionMock.applyConfiguration(additionalConfigs);
        return diagramRecognitionMock;
    }

    @Override
    protected void initializeState() {
        var inputDiagramsMock = getDataRepository().getData(InputDiagramDataMock.ID, InputDiagramDataMock.class);
        if (inputDiagramsMock.isEmpty()) {
            return;
        }
        logger.info("Creating DiagramRecognitionMock State");
        var diagramRecognitionState = new DiagramRecognitionStateImpl();
        var diagrams = inputDiagramsMock.get().getDiagramProject().getDiagramsFromGoldstandard();
        for (var diagram : diagrams) {
            logger.debug("Loaded Diagram {}", diagram.getPath());
            diagramRecognitionState.addDiagram(diagram);
        }
        getDataRepository().addData(DiagramRecognitionState.ID, diagramRecognitionState);
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return new ArrayList<>();
    }
}