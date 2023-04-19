/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

/**
 * The Pipeline defines the execution of the agents.
 */
public final class ArDoCo extends Pipeline {

    private static final Logger classLogger = LoggerFactory.getLogger(ArDoCo.class);

    private final String projectName;

    /**
     * Default constructor to adhere simplify tests that do not care about the project's name. Additionally, it is needed for testing the configurations
     */
    private ArDoCo() {
        this("");
    }

    /**
     * Creates a new instance of ArDoCo. The provided name should be the project's name and will be used to identify spots within the text where the project is
     * mentioned.
     *
     * @param projectName the project's name
     */
    public ArDoCo(String projectName) {
        super("ArDoCo", new DataRepository());
        this.projectName = projectName;
        initDataRepository();
    }

    /**
     * Returns a new instance of this class based with the given project name
     *
     * @param projectName the project name
     * @return a new instance of ArDoCo
     */
    public static ArDoCo getInstance(String projectName) {
        return new ArDoCo(projectName);
    }

    private void initDataRepository() {
        ProjectPipelineData projectPipelineData = new ProjectPipelineDataImpl(projectName);
        getDataRepository().addData(ProjectPipelineData.ID, projectPipelineData);
    }

    @Override
    public DataRepository getDataRepository() {
        return super.getDataRepository();
    }

    public ArDoCoResult runAndSave(File outputDir) {
        classLogger.info("Starting {}", this.projectName);

        if (!this.hasPipelineSteps()) {
            logger.error("Pipeline has not been defined and initialized beforehand. Aborting!");
            return null;
        }

        var startTime = System.currentTimeMillis();
        this.run();
        var duration = Duration.ofMillis(System.currentTimeMillis() - startTime);

        ArDoCoResult arDoCoResult = new ArDoCoResult(this.getDataRepository());
        saveOutput(this.projectName, outputDir, arDoCoResult);

        classLogger.info("Finished in {}.{}s.", duration.getSeconds(), duration.toMillisPart());
        return arDoCoResult;
    }

    /**
     * This method sets up the pipeline for ArDoCo.
     *
     * @param inputText              The input text file
     * @param inputArchitectureModel the input architecture file
     * @param architectureModelType  the type of the architecture (e.g., PCM, UML)
     * @param inputCodeModel         the input code model file
     * @param additionalConfigs      the additional configs
     * @throws IOException When one of the input files cannot be accessed/loaded
     */
    public void definePipeline(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCodeModel,
            Map<String, String> additionalConfigs) throws IOException {
        var dataRepository = this.getDataRepository();
        var text = CommonUtilities.readInputText(inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);

        this.addPipelineStep(PipelineUtils.getTextPreprocessing(additionalConfigs, dataRepository));
        this.addPipelineStep(PipelineUtils.getArchitectureModelProvider(inputArchitectureModel, architectureModelType, dataRepository));
        if (inputCodeModel != null) {
            this.addPipelineStep(PipelineUtils.getJavaModelProvider(inputCodeModel, dataRepository));
        }
        this.addPipelineStep(PipelineUtils.getTextExtraction(additionalConfigs, dataRepository));
        this.addPipelineStep(PipelineUtils.getRecommendationGenerator(additionalConfigs, dataRepository));
        this.addPipelineStep(PipelineUtils.getConnectionGenerator(additionalConfigs, dataRepository));
        this.addPipelineStep(PipelineUtils.getInconsistencyChecker(additionalConfigs, dataRepository));
    }

    private static void saveOutput(String name, File outputDir, ArDoCoResult arDoCoResult) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(outputDir);
        Objects.requireNonNull(arDoCoResult);

        classLogger.info("Starting to write output...");
        FilePrinter.writeTraceabilityLinkRecoveryOutput(getOutputFile(name, outputDir, "traceLinks_"), arDoCoResult);
        FilePrinter.writeInconsistencyOutput(getOutputFile(name, outputDir, "inconsistencyDetection_"), arDoCoResult);
        classLogger.info("Finished to write output.");
    }

    private static File getOutputFile(String name, File outputDir, String prefix) {
        var filename = prefix + name + ".txt";
        var filepath = outputDir.toPath().resolve(filename);
        return filepath.toFile();
    }

}
