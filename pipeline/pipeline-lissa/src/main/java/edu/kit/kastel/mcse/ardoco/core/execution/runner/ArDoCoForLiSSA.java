/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;

/**
 * The Runner for the Linking Sketches and Software Architecture Approach (LiSSA)
 */
public class ArDoCoForLiSSA extends ArDoCoRunner {

    public ArDoCoForLiSSA(String projectName) {
        super(projectName);
    }

    public void setUp(File diagramDirectory, File inputText, File inputModelArchitecture, ArchitectureModelType inputArchitectureModelType,
            SortedMap<String, String> additionalConfigs, File outputDir) {
        definePipeline(diagramDirectory, inputText, inputModelArchitecture, inputArchitectureModelType, additionalConfigs);
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    public void setUp(String diagramDirectory, String inputTextLocation, String inputArchitectureModelLocation, ArchitectureModelType architectureModelType,
            SortedMap<String, String> additionalConfigs, String outputDirectory) {
        setUp(new File(diagramDirectory), new File(inputTextLocation), new File(inputArchitectureModelLocation), architectureModelType, additionalConfigs,
                new File(outputDirectory));
    }

    /**
     * This method sets up the pipeline for ArDoCo.
     *
     * @param diagramDirectory       the directory that contain the diagram files.
     * @param inputText              the input text file
     * @param inputArchitectureModel the input architecture file
     * @param architectureModelType  the type of the architecture (e.g., PCM, UML)
     * @param additionalConfigs      the additional configs
     */
    private void definePipeline(File diagramDirectory, File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType,
            SortedMap<String, String> additionalConfigs) {
        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();
        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        DataRepositoryHelper.putDiagramDirectory(dataRepository, diagramDirectory);

        arDoCo.addPipelineStep(DiagramRecognition.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));

        ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.get(inputArchitectureModel, architectureModelType, null, additionalConfigs,
                dataRepository);
        arDoCo.addPipelineStep(arCoTLModelProviderAgent);

        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(InconsistencyChecker.get(additionalConfigs, dataRepository));
    }
}
