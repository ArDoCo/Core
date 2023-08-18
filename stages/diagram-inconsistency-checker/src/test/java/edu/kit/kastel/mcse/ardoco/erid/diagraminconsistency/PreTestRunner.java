package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ParameterizedRunner;
import edu.kit.kastel.mcse.ardoco.core.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class PreTestRunner extends ParameterizedRunner<PreTestRunner.Parameters> {

    protected PreTestRunner(String projectName, Parameters parameters) {
        super(projectName, parameters);
    }

    public record Parameters(DiagramProject diagramProject, File outputDir, boolean useMockDiagrams) {
    }

    @Override
    public List<AbstractPipelineStep> initializePipelineSteps(Parameters p) throws IOException {
        var pipelineSteps = new ArrayList<AbstractPipelineStep>();

        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        var text = CommonUtilities.readInputText(p.diagramProject.getTextFile());
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        pipelineSteps.add(TextPreprocessingAgent.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));

        pipelineSteps.add(ModelProviderAgent.get(p.diagramProject.getModelFile(), p.diagramProject.getArchitectureModelType(), dataRepository));

        if (p.useMockDiagrams) {
            pipelineSteps.add(new DiagramRecognitionMock(p.diagramProject, p.diagramProject.getAdditionalConfigurations(), dataRepository));
        } else {
            pipelineSteps.add(DiagramRecognition.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));
        }

        pipelineSteps.add(TextExtraction.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));
        pipelineSteps.add(RecommendationGenerator.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));
        pipelineSteps.add(new DiagramConnectionGenerator(p.diagramProject.getAdditionalConfigurations(), dataRepository));

        return pipelineSteps;
    }
}
