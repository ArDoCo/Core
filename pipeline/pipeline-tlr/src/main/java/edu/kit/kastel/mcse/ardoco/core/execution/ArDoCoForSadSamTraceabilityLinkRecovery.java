/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.models.ArCoTLModelProviderAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

public class ArDoCoForSadSamTraceabilityLinkRecovery extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForSadSamTraceabilityLinkRecovery.class);

    public ArDoCoForSadSamTraceabilityLinkRecovery(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCode, Map<String, String> additionalConfigs,
            File outputDir) {
        try {
            definePipeline(inputText, inputArchitectureModel, architectureModelType, inputCode, additionalConfigs);
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return;
        }
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    public void setUp(String inputTextLocation, String inputArchitectureModelLocation, ArchitectureModelType architectureModelType, String inputCodeLocation,
            Map<String, String> additionalConfigs, String outputDirectory) {
        setUp(new File(inputTextLocation), new File(inputArchitectureModelLocation), architectureModelType, new File(inputCodeLocation), additionalConfigs, new File(outputDirectory));
    }

    private void definePipeline(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCode, Map<String, String> additionalConfigs)
            throws IOException {
        var dataRepository = this.getArDoCo().getDataRepository();
        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);

        this.getArDoCo().addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));
        this.getArDoCo().addPipelineStep(ArCoTLModelProviderAgent.get(inputArchitectureModel, architectureModelType, inputCode, additionalConfigs, dataRepository));

        this.getArDoCo().addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        this.getArDoCo().addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        this.getArDoCo().addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
    }
}
