/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.SadCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.models.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramToModel;

public class ArDoCoSadDiagramCodeTraceabilityLinkRecovery extends ArDoCoRunner {
    public ArDoCoSadDiagramCodeTraceabilityLinkRecovery(String projectName) {
        super(projectName);
    }

    public void setUp(File inputText, File inputDiagramDirectory, ArchitectureModelType architectureModelType, File inputCode,
            Map<String, String> additionalConfigs, File outputDir) {

        definePipeline(inputText, inputDiagramDirectory, architectureModelType, inputCode, additionalConfigs);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    private void definePipeline(File inputText, File inputDiagramDirectory, ArchitectureModelType architectureModelType, File inputCode,
            Map<String, String> additionalConfigs) {
        ArDoCo arDoCo = this.getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.get(null, null, inputCode, additionalConfigs, dataRepository);
        arDoCo.addPipelineStep(arCoTLModelProviderAgent);
        arDoCo.addPipelineStep(DiagramRecognition.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(DiagramToModel.get(additionalConfigs, dataRepository));

        arDoCo.addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));

        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(SadCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));
    }
}
