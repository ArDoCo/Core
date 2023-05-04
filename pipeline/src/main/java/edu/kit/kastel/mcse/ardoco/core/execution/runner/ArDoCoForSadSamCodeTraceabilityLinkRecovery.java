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
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.PipelineUtils;
import edu.kit.kastel.mcse.ardoco.core.models.ArCoTLModelProviderAgent;

public class ArDoCoForSadSamCodeTraceabilityLinkRecovery extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForSadSamCodeTraceabilityLinkRecovery.class);

    public ArDoCoForSadSamCodeTraceabilityLinkRecovery(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCode,
            Map<String, String> additionalConfigs, File outputDir) {
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

    private void definePipeline(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCode,
            Map<String, String> additionalConfigs) throws IOException {
        ArDoCo arDoCo = this.getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);

        arDoCo.addPipelineStep(PipelineUtils.getTextPreprocessing(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(PipelineUtils.getArchitectureModelProvider(inputArchitectureModel, architectureModelType, dataRepository));

        arDoCo.addPipelineStep(PipelineUtils.getTextExtraction(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(PipelineUtils.getRecommendationGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(PipelineUtils.getConnectionGenerator(additionalConfigs, dataRepository));

        ArCoTLModelProviderAgent arCoTLModelProviderAgent = PipelineUtils.getArCoTLModelProviderAgent(inputArchitectureModel, architectureModelType, inputCode,
                additionalConfigs, dataRepository);
        arDoCo.addPipelineStep(arCoTLModelProviderAgent);
        arDoCo.addPipelineStep(PipelineUtils.getSamCodeTraceabilityLinkRecovery(additionalConfigs, dataRepository));

        arDoCo.addPipelineStep(PipelineUtils.getSadSamCodeTraceabilityLinkRecovery(additionalConfigs, dataRepository));
    }
}
