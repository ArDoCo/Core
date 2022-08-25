/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.api.data.ProjectPipelineData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.TextProvider;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.JavaJsonModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.UMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

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

    /**
     * Run the approach with the given parameters.
     *
     * @param name                   Name of the run
     * @param inputText              File of the input text.
     * @param inputArchitectureModel File of the input model (PCM or UML)
     * @param architectureModel      the architecture model to use
     * @return the {@link ArDoCoResult} that contains the blackboard with all results (of all steps)
     */
    public static ArDoCoResult run(String name, File inputText, File inputArchitectureModel, ArchitectureModelType architectureModel, File additionalConfigs) {
        ArDoCo arDoCo = getInstance(name);
        return arDoCo.runAndSave(name, inputText, inputArchitectureModel, architectureModel, null, additionalConfigs, null);
    }

    /**
     * Run the approach with the given parameters and save the output to the file system.
     *
     * @param name                   Name of the run
     * @param inputText              File of the input text.
     * @param inputArchitectureModel File of the input model (PCM or UML)
     * @param architectureModelType  the architecture model to use
     * @param inputCodeModel         File of the input model (Java Code JSON)
     * @param additionalConfigsFile  File with the additional or overwriting config parameters that should be used
     * @param outputDir              File that represents the output directory where the results should be written to
     * @return the {@link ArDoCoResult} that contains the blackboard with all results (of all steps)
     */
    public ArDoCoResult runAndSave(String name, File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCodeModel,
            File additionalConfigsFile, File outputDir) {
        classLogger.info("Loading additional configs ..");
        var additionalConfigs = loadAdditionalConfigs(additionalConfigsFile);

        classLogger.info("Starting {}", name);
        var startTime = System.currentTimeMillis();

        try {
            definePipeline(inputText, inputArchitectureModel, architectureModelType, inputCodeModel, additionalConfigs);
        } catch (IOException e) {
            classLogger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            return null;
        }
        this.run();

        // save step
        var duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
        ArDoCoResult arDoCoResult = new ArDoCoResult(this.getDataRepository());
        saveOutput(name, outputDir, arDoCoResult);

        classLogger.info("Finished in {}.{}s.", duration.getSeconds(), duration.toMillisPart());
        return arDoCoResult;
    }

    public void definePipeline(File inputText, File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCodeModel,
            Map<String, String> additionalConfigs) throws IOException {
        var dataRepository = this.getDataRepository();

        this.addPipelineStep(getTextProvider(inputText, additionalConfigs, this.getDataRepository()));
        this.addPipelineStep(getArchitectureModelProvider(inputArchitectureModel, architectureModelType, dataRepository));
        if (inputCodeModel != null) {
            this.addPipelineStep(getJavaModelProvider(inputCodeModel, dataRepository));
        }
        this.addPipelineStep(getTextExtraction(additionalConfigs, dataRepository));
        this.addPipelineStep(getRecommendationGenerator(additionalConfigs, dataRepository));
        this.addPipelineStep(getConnectionGenerator(additionalConfigs, dataRepository));
        this.addPipelineStep(getInconsistencyChecker(additionalConfigs, dataRepository));
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

    /**
     * Creates an {@link InconsistencyChecker} and applies the additional configuration to it.
     * 
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of InconsistencyChecker
     */
    public static InconsistencyChecker getInconsistencyChecker(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var inconsistencyChecker = new InconsistencyChecker(dataRepository);
        inconsistencyChecker.applyConfiguration(additionalConfigs);
        return inconsistencyChecker;
    }

    /**
     * Creates a {@link ConnectionGenerator} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of connectionGenerator
     */
    public static ConnectionGenerator getConnectionGenerator(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var connectionGenerator = new ConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(additionalConfigs);
        return connectionGenerator;
    }

    /**
     * Creates a {@link RecommendationGenerator} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of {@link RecommendationGenerator}
     */
    public static RecommendationGenerator getRecommendationGenerator(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var recommendationGenerator = new RecommendationGenerator(dataRepository);
        recommendationGenerator.applyConfiguration(additionalConfigs);
        return recommendationGenerator;
    }

    /**
     * Creates a {@link TextExtraction} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of InconsistencyChecker
     */
    public static TextExtraction getTextExtraction(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var textExtractor = new TextExtraction(dataRepository);
        textExtractor.applyConfiguration(additionalConfigs);
        return textExtractor;
    }

    /**
     * Creates a {@link ModelProvider} for Java.
     * 
     * @param inputCodeModel the path to the input Java Code Model
     * @param dataRepository the data repository
     * @return A ModelProvider for the Java Code Model
     * @throws IOException if the Code Model cannot be accessed
     */
    public static ModelProvider getJavaModelProvider(File inputCodeModel, DataRepository dataRepository) throws IOException {
        ModelConnector javaModel = new JavaJsonModelConnector(inputCodeModel);
        return new ModelProvider(dataRepository, javaModel);
    }

    /**
     * Creates a {@link ModelProvider} for PCM.
     *
     * @param inputArchitectureModel the path to the input PCM
     * @param architectureModelType  the architecture model to use
     * @param dataRepository         the data repository
     * @return A ModelProvider for the PCM
     * @throws IOException if the Code Model cannot be accessed
     */
    public static ModelProvider getArchitectureModelProvider(File inputArchitectureModel, ArchitectureModelType architectureModelType,
            DataRepository dataRepository) throws IOException {
        ModelConnector connector = switch (architectureModelType) {
        case PCM -> new PcmXMLModelConnector(inputArchitectureModel);
        case UML -> new UMLModelConnector(inputArchitectureModel);
        };
        return new ModelProvider(dataRepository, connector);
    }

    /**
     * Creates a {@link CoreNLPProvider} as {@link edu.kit.kastel.mcse.ardoco.core.api.data.text.TextProvider} and reads the provided text.
     * 
     * @param inputText         the text that should be read
     * @param additionalConfigs the additional configuration that should be applied
     * @param dataRepository    the data repository
     * @return a CoreNLPProvider with the provided text read in
     * @throws FileNotFoundException if the text file cannot be found
     */
    public static TextProvider getTextProvider(File inputText, Map<String, String> additionalConfigs, DataRepository dataRepository)
            throws FileNotFoundException {
        var textProvider = new CoreNLPProvider(dataRepository, new FileInputStream(inputText));
        textProvider.applyConfiguration(additionalConfigs);
        return textProvider;
    }

    /**
     * Loads the file that contains additional configurations and returns the Map that consists of the configuration options.
     * 
     * @param additionalConfigsFile the file containing the additional configurations
     * @return a Map with the additional configurations
     */
    public static Map<String, String> loadAdditionalConfigs(File additionalConfigsFile) {
        Map<String, String> additionalConfigs = new HashMap<>();
        if (additionalConfigsFile != null && additionalConfigsFile.exists()) {
            try (var scanner = new Scanner(additionalConfigsFile, StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    var line = scanner.nextLine();
                    if (line == null || line.isBlank()) {
                        continue;
                    }
                    var values = line.split(KEY_VALUE_CONNECTOR, 2);
                    if (values.length != 2) {
                        classLogger.error(
                                "Found config line \"{}\". Layout has to be: 'KEY" + KEY_VALUE_CONNECTOR + "VALUE', e.g., 'SimpleClassName" + CLASS_ATTRIBUTE_CONNECTOR + "AttributeName" + KEY_VALUE_CONNECTOR + "42",
                                line);
                    } else {
                        additionalConfigs.put(values[0], values[1]);
                    }
                }
            } catch (IOException e) {
                classLogger.error(e.getMessage(), e);
            }
        }
        return additionalConfigs;
    }
}
