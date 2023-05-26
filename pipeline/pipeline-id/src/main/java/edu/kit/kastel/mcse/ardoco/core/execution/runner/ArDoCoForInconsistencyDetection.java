/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ArDoCoForInconsistencyDetection extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForInconsistencyDetection.class);

    public ArDoCoForInconsistencyDetection(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, File inputModelArchitecture, ArchitectureModelType inputArchitectureModelType, Map<String, String> additionalConfigs,
            File outputDir) {
        try {
            definePipeline(inputText, inputModelArchitecture, inputArchitectureModelType, additionalConfigs);
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return;
        }
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    public void setUp(String inputTextLocation, String inputArchitectureModelLocation, ArchitectureModelType architectureModelType,
            Map<String, String> additionalConfigs, String outputDirectory) {
        setUp(new File(inputTextLocation), new File(inputArchitectureModelLocation), architectureModelType, additionalConfigs, new File(outputDirectory));
    }

    /**
     * This method sets up the pipeline for ArDoCo.
     *
     * @param inputText              The input text file
     * @param inputArchitectureModel the input architecture file
     * @param architectureModelType  the type of the architecture (e.g., PCM, UML)
     * @param additionalConfigs      the additional configs
     * @throws IOException When one of the input files cannot be accessed/loaded
     */
    private void definePipeline(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, Map<String, String> additionalConfigs)
            throws IOException {
        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();
        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);

        arDoCo.addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ModelProviderAgent.get(inputArchitectureModel, architectureModelType, dataRepository));
        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(InconsistencyChecker.get(additionalConfigs, dataRepository));
    }
}
