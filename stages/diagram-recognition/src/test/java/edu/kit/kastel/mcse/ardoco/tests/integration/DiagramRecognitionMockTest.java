/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tests.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.collections.impl.factory.SortedMaps;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagrams;
import edu.kit.kastel.mcse.ardoco.tests.eval.StageTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DiagramRecognitionMockTest extends StageTest<DiagramRecognitionMock, GoldStandardDiagrams, DiagramRecognitionMockTest.DiagramRecognitionResult> {
    public DiagramRecognitionMockTest() {
        super(new DiagramRecognitionMock(null, SortedMaps.mutable.empty(), null), DiagramProject.values());
    }

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

    @Override
    protected DiagramRecognitionResult runComparable(GoldStandardDiagrams project, SortedMap<String, String> additionalConfigurations, boolean cachePreRun) {
        var result = run(project, additionalConfigurations, cachePreRun);
        var diagramRecognition = result.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).orElseThrow();
        var diagrams = diagramRecognition.getDiagrams();
        return new DiagramRecognitionResult(diagrams);
    }

    @Override
    protected DataRepository runPreTestRunner(@NotNull GoldStandardDiagrams project) {
        return new AnonymousRunner(project.getProjectName()) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) throws IOException {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();
                pipelineSteps.add(ModelProviderAgent.get(project.getModelFile(), ArchitectureModelType.PCM, dataRepository));
                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    @Override
    protected DataRepository runTestRunner(@NotNull GoldStandardDiagrams project, @NotNull SortedMap<String, String> additionalConfigurations,
            @NotNull DataRepository preRunDataRepository) {
        return new AnonymousRunner(project.getProjectName(), preRunDataRepository) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();
                pipelineSteps.add(new DiagramRecognitionMock(project, project.getAdditionalConfigurations(), dataRepository));
                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    public record DiagramRecognitionResult(List<Diagram> diagrams) {
    }
}
