package edu.kit.kastel.mcse.ardoco.tests;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramDataMock;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunnerExt;
import edu.kit.kastel.mcse.ardoco.core.models.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.erid.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class PreTestRunner extends ArDoCoRunnerExt<PreTestRunner.Parameters> {
    public PreTestRunner(String projectName) {
        super(projectName);
    }

    public record Parameters(DiagramProject diagramProject, File outputDir, boolean useMockDiagrams) {
    }

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    @Override
    public boolean setUp(Parameters p) {
        try {
            definePipeline(p);
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return false;
        }
        setOutputDirectory(p.outputDir);
        isSetUp = true;
        return true;
    }

    private void definePipeline(Parameters p) throws IOException {
        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        if (p.useMockDiagrams) {
            var data = new InputDiagramDataMock(p.diagramProject);
            dataRepository.addData(InputDiagramDataMock.ID, data);
            arDoCo.addPipelineStep(DiagramRecognitionMock.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));
        } else {
            arDoCo.addPipelineStep(DiagramRecognition.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));
        }

        var text = CommonUtilities.readInputText(p.diagramProject.getTextFile());
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        arDoCo.addPipelineStep(TextPreprocessingAgent.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));

        arDoCo.addPipelineStep(ModelProviderAgent.get(p.diagramProject.getModelFile(), p.diagramProject.getArchitectureModelType(), dataRepository));
        arDoCo.addPipelineStep(TextExtraction.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));
    }
}
