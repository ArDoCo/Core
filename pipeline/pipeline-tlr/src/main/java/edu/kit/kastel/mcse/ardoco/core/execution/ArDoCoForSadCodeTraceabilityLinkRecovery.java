/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.codetraceability.SadCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.models.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

public class ArDoCoForSadCodeTraceabilityLinkRecovery extends ArDoCoRunner {

    public ArDoCoForSadCodeTraceabilityLinkRecovery(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, File inputCode, SortedMap<String, String> additionalConfigs, File outputDir) {
        definePipeline(inputText, inputCode, additionalConfigs);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    private void definePipeline(File inputText, File inputCode, SortedMap<String, String> additionalConfigs) {
        ArDoCo arDoCo = this.getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.get(null, null, inputCode, additionalConfigs, dataRepository);
        arDoCo.addPipelineStep(arCoTLModelProviderAgent);

        arDoCo.addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));

        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(SadCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));
    }
}
