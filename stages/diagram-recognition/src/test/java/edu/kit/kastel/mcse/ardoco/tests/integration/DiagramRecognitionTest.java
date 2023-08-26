package edu.kit.kastel.mcse.ardoco.tests.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognitionStateImpl;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagrams;
import edu.kit.kastel.mcse.ardoco.tests.eval.StageTest;

public class DiagramRecognitionTest extends StageTest<DiagramRecognition, GoldStandardDiagrams, DiagramRecognitionTest.DiagramRecognitionResult> {

    public DiagramRecognitionTest() {
        super(new DiagramRecognition(new DiagramRecognitionStateImpl(), new DataRepository()), DiagramProject.values());
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
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) throws IOException {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();
                pipelineSteps.add(ModelProviderAgent.get(project.getModelFile(), ArchitectureModelType.PCM, dataRepository));
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
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();
                dataRepository.addData(InputDiagramData.ID, new InputDiagramData(project.getDiagramData()));
                pipelineSteps.add(new DiagramRecognition(new DiagramRecognitionStateImpl(), dataRepository));
                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    public record DiagramRecognitionResult(List<Diagram> diagrams) {
    }
}
