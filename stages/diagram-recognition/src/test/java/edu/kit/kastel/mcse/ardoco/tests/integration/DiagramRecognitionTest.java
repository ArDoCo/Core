/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tests.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramGS;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.common.util.Comparators;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagrams;
import edu.kit.kastel.mcse.ardoco.tests.eval.StageTest;

public class DiagramRecognitionTest extends StageTest<DiagramRecognition, GoldStandardDiagrams, DiagramRecognitionTest.DiagramRecognitionResult> {

    public DiagramRecognitionTest() {
        super(new DiagramRecognition(new DataRepository()), DiagramProject.values());
    }

    @DisplayName("Evaluate Diagram Recognition")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(1)
    void evaluateNonHistoricalDiagramRecognition(DiagramProject project) {
        var result = runComparable(project);
        assertTrue(Comparators.collectionsEqualsAnyOrder(result.diagrams.stream().map(Diagram::getShortResourceName).toList(), project.getDiagramsGoldStandard()
                .stream()
                .map(DiagramGS::getShortResourceName)
                .toList()));
    }

    @DisplayName("Evaluate Diagram Recognition (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(2)
    void evaluateHistoricalDiagramRecognition(DiagramProject project) {
        var result = runComparable(project);
        assertTrue(Comparators.collectionsEqualsAnyOrder(result.diagrams.stream().map(Diagram::getShortResourceName).toList(), project.getDiagramsGoldStandard()
                .stream()
                .map(DiagramGS::getShortResourceName)
                .toList()));
    }

    @Override
    protected DiagramRecognitionResult runComparable(GoldStandardDiagrams project, SortedMap<String, String> additionalConfigurations, boolean cachePreRun) {
        var result = run(project, additionalConfigurations, cachePreRun);
        var diagramRecognition = result.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).orElseThrow();
        var diagrams = diagramRecognition.getDiagrams();
        return new DiagramRecognitionResult(diagrams);
    }

    @Override
    protected DataRepository runPreTestRunner(GoldStandardDiagrams project) {
        return new AnonymousRunner(project.getProjectName()) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) {
                DataRepositoryHelper.getMetaData(dataRepository).getWordSimUtils().setConsiderAbbreviations(true);
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();
                ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.get(project.getModelFile(), ArchitectureModelType.PCM, null,
                        project.getAdditionalConfigurations(), dataRepository);
                pipelineSteps.add(arCoTLModelProviderAgent);
                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    @Override
    protected DataRepository runTestRunner(GoldStandardDiagrams project, SortedMap<String, String> additionalConfigurations,
            DataRepository preRunDataRepository) {
        return new AnonymousRunner(project.getProjectName(), preRunDataRepository) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) {
                DataRepositoryHelper.getMetaData(dataRepository).getWordSimUtils().setConsiderAbbreviations(true);
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();
                dataRepository.addData(InputDiagramData.ID, new InputDiagramData(project.getDiagramData()));
                pipelineSteps.add(new DiagramRecognition(dataRepository));
                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    public record DiagramRecognitionResult(List<Diagram> diagrams) {
    }
}
