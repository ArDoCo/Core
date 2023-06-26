package runner;

import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;

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
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.models.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.erid.DiagramRecognitionMock;

public class ArDoCoForERID extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForERID.class);

    public ArDoCoForERID(String projectName) {
        super(projectName);
    }

    public void setUp(File goldStandardDirectory, File inputText, File inputModelArchitecture, ArchitectureModelType inputArchitectureModelType,
            Map<String, String> additionalConfigs, File outputDir) {
        try {
            definePipeline(goldStandardDirectory, inputText, inputModelArchitecture, inputArchitectureModelType, additionalConfigs, outputDir);
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return;
        }
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    public void setUp(String goldStandardDirectory, String inputText, String inputModelArchitecture, ArchitectureModelType architectureModelType,
            Map<String, String> additionalConfigs, String outputDir) {
        setUp(new File(goldStandardDirectory), new File(inputText), new File(inputModelArchitecture), architectureModelType, additionalConfigs,
                new File(outputDir));
    }

    private void definePipeline(File goldStandardDirectory, File inputText, File inputModelArchitecture, ArchitectureModelType architectureModelType,
            Map<String, String> additionalConfigs, File outputDir) throws IOException {
        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();
        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }

        DataRepositoryHelper.putInputText(dataRepository, text);
        var data = new InputDiagramDataMock(goldStandardDirectory);
        dataRepository.addData(data.ID, data);

        arDoCo.addPipelineStep(DiagramRecognitionMock.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ModelProviderAgent.get(inputModelArchitecture, architectureModelType, dataRepository));
        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(InconsistencyChecker.get(additionalConfigs, dataRepository));
    }
}
