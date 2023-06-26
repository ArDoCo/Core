package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramDataMock;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.models.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.erid.DiagramRecognitionMock;

public class ArDoCoForERID extends ArDoCoRunnerExt<ArDoCoForERID.Parameters> {
    public record Parameters(File goldStandardDirectory, File inputText, File inputModelArchitecture, ArchitectureModelType inputArchitectureModelType,
                             Map<String, String> additionalConfigs, File outputDir) {
    }

    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForERID.class);

    public ArDoCoForERID(String projectName) {
        super(projectName);
    }

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
        var text = CommonUtilities.readInputText(p.inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }

        DataRepositoryHelper.putInputText(dataRepository, text);
        var data = new InputDiagramDataMock(p.goldStandardDirectory);
        dataRepository.addData(data.ID, data);

        arDoCo.addPipelineStep(DiagramRecognitionMock.get(p.additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(TextPreprocessingAgent.get(p.additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ModelProviderAgent.get(p.inputModelArchitecture, p.inputArchitectureModelType, dataRepository));
        arDoCo.addPipelineStep(TextExtraction.get(p.additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(p.additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(DiagramConnectionGenerator.get(p.additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(p.additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(InconsistencyChecker.get(p.additionalConfigs, dataRepository));
    }
}
