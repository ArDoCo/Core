package edu.kit.kastel.mcse.ardoco.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramrecognitionmock.InputDiagramDataMock;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognitionmock.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DiagramRecognitionTest {
    @DisplayName("Evaluate Diagram Recognition")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(1)
    void evaluateNonHistoricalDiagramRecognition(DiagramProject project) {
        run(project);
    }

    @DisplayName("Evaluate Diagram Recognition (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(2)
    void evaluateHistoricalDiagramRecognition(DiagramProject project) {
        run(project);
    }

    private void run(DiagramProject project) {
        var dataRepository = new AnonymousRunner(project.name()) {
            @Override
            public void initializePipelineSteps() {
                ArDoCo arDoCo = getArDoCo();
                var dataRepository = arDoCo.getDataRepository();

                var data = new InputDiagramDataMock(project);
                dataRepository.addData(InputDiagramDataMock.ID, data);

                arDoCo.addPipelineStep(new DiagramRecognitionMock(project.getProject().getAdditionalConfigurations(), dataRepository));
            }
        }.runWithoutSaving();
        var diagramRecognition = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).orElseThrow();
        var diagrams = diagramRecognition.getDiagrams();
        Assertions.assertTrue(diagrams.size() > 0);
    }
}
