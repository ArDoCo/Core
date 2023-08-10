package edu.kit.kastel.mcse.ardoco.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ParameterizedRunner;
import edu.kit.kastel.mcse.ardoco.core.models.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.DiagramBackedTextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextStateImpl;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramrecognitionmock.InputDiagramDataMock;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognitionmock.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class PreTestRunner extends ParameterizedRunner<PreTestRunner.Parameters> {
    public PreTestRunner(String projectName, Parameters parameters) {
        super(projectName, parameters);
    }

    public record Parameters(DiagramProject diagramProject, File outputDir, boolean useMockDiagrams) {
    }

    @Override
    public List<AbstractPipelineStep> initializePipelineSteps(Parameters p) throws IOException {
        var pipelineSteps = new ArrayList<AbstractPipelineStep>();

        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        if (p.useMockDiagrams) {
            var data = new InputDiagramDataMock(p.diagramProject);
            dataRepository.addData(InputDiagramDataMock.ID, data);
            pipelineSteps.add(new DiagramRecognitionMock(p.diagramProject.getProject().getAdditionalConfigurations(), dataRepository));
        } else {
            pipelineSteps.add(DiagramRecognition.get(p.diagramProject.getProject().getAdditionalConfigurations(), dataRepository));
        }

        var text = CommonUtilities.readInputText(p.diagramProject.getProject().getTextFile());
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        pipelineSteps.add(TextPreprocessingAgent.get(p.diagramProject.getProject().getAdditionalConfigurations(), dataRepository));

        pipelineSteps.add(ModelProviderAgent.get(p.diagramProject.getProject().getModelFile(), p.diagramProject.getArchitectureModelType(), dataRepository));

        var textState = new TextStateImpl();
        var textStrategy = new DiagramBackedTextStateStrategy(textState, dataRepository);
        textState.setTextStateStrategy(textStrategy);
        dataRepository.addData(TextState.ID, textState);
        pipelineSteps.add(TextExtraction.get(p.diagramProject.getProject().getAdditionalConfigurations(), dataRepository));

        pipelineSteps.add(RecommendationGenerator.get(p.diagramProject.getProject().getAdditionalConfigurations(), dataRepository));
        return pipelineSteps;
    }
}
