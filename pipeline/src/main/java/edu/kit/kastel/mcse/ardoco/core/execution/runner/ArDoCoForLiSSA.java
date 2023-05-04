/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.PipelineUtils;

/**
 * The Runner for the Linking Sketches and Software Architecture Approach (LiSSA)
 */
public class ArDoCoForLiSSA extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForLiSSA.class);

    public ArDoCoForLiSSA(String projectName) {
        super(projectName);
    }

    public void setUp(File diagramDirectory, File inputText, File inputModelArchitecture, ArchitectureModelType inputArchitectureModelType,
            Map<String, String> additionalConfigs, File outputDir) {
        try {
            definePipeline(diagramDirectory, inputText, inputModelArchitecture, inputArchitectureModelType, additionalConfigs);
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return;
        }
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    public void setUp(String diagramDirectory, String inputTextLocation, String inputArchitectureModelLocation, ArchitectureModelType architectureModelType,
            Map<String, String> additionalConfigs, String outputDirectory) {
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
     * @throws IOException When one of the input files cannot be accessed/loaded
     */
    private void definePipeline(File diagramDirectory, File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType,
            Map<String, String> additionalConfigs) throws IOException {
        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();
        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        DataRepositoryHelper.putDiagramDirectory(dataRepository, diagramDirectory);

        arDoCo.addPipelineStep(PipelineUtils.getDiagramDetection(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(PipelineUtils.getTextPreprocessing(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(PipelineUtils.getArchitectureModelProvider(inputArchitectureModel, architectureModelType, dataRepository));
        arDoCo.addPipelineStep(PipelineUtils.getTextExtraction(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(PipelineUtils.getRecommendationGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(PipelineUtils.getConnectionGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(PipelineUtils.getInconsistencyChecker(additionalConfigs, dataRepository));
    }
}
