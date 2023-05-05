/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;

public class ArDoCoForSadSamTraceabilityLinkRecovery extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForSadSamTraceabilityLinkRecovery.class);

    public ArDoCoForSadSamTraceabilityLinkRecovery(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, Map<String, String> additionalConfigs,
            File outputDir) {
        try {
            definePipeline(inputText, inputArchitectureModel, architectureModelType, additionalConfigs);
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

    private void definePipeline(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, Map<String, String> additionalConfigs)
            throws IOException {
        var dataRepository = this.getArDoCo().getDataRepository();
        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);

        this.getArDoCo().addPipelineStep(PipelineUtils.getTextPreprocessing(additionalConfigs, dataRepository));
        this.getArDoCo().addPipelineStep(PipelineUtils.getArchitectureModelProvider(inputArchitectureModel, architectureModelType, dataRepository));

        this.getArDoCo().addPipelineStep(PipelineUtils.getTextExtraction(additionalConfigs, dataRepository));
        this.getArDoCo().addPipelineStep(PipelineUtils.getRecommendationGenerator(additionalConfigs, dataRepository));
        this.getArDoCo().addPipelineStep(PipelineUtils.getConnectionGenerator(additionalConfigs, dataRepository));
    }
}
