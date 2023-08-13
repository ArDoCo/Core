package edu.kit.kastel.mcse.ardoco.erid.diagramrecognitionmock;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognitionStateImpl;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagramsWithTLR;

public class DiagramRecognitionMock extends ExecutionStage {
    private final GoldStandardDiagramsWithTLR goldStandardProject;

    public DiagramRecognitionMock(GoldStandardDiagramsWithTLR goldStandardProject, Map<String, String> additionalConfigs, DataRepository dataRepository) {
        super("DiagramRecognitionMock", dataRepository, List.of(), additionalConfigs);
        this.goldStandardProject = goldStandardProject;
    }

    @Override
    protected void initializeState() {
        logger.info("Creating DiagramRecognitionMock State");
        var diagramRecognitionState = new DiagramRecognitionStateImpl();
        var diagrams = goldStandardProject.getDiagrams();
        for (var diagram : diagrams) {
            logger.debug("Loaded Diagram {}", diagram.getResourceName());
            diagramRecognitionState.addDiagram(diagram);
        }
        getDataRepository().addData(DiagramRecognitionState.ID, diagramRecognitionState);
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return List.of();
    }
}
