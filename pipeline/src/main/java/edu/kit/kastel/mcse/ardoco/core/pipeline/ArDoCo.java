/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import static edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.JavaJsonModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.helpers.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

/**
 * The Pipeline defines the execution of the agents.
 */
public final class ArDoCo extends Pipeline {
    private DataStructure dataStructure;

    private static final Logger logger = LoggerFactory.getLogger(ArDoCo.class);

    public ArDoCo() {
        super("ArDoCo", new DataRepository());
    }

    private void setDataStructure(DataStructure dataStructure) {
        this.dataStructure = dataStructure;
    }

    public DataStructure getDataStructure() {
        return this.dataStructure;
    }

    /**
     * Run the approach with the given parameters.
     *
     * @param name                   Name of the run
     * @param inputText              File of the input text.
     * @param inputArchitectureModel File of the input model (PCM)
     * @return the {@link DataStructure} that contains the blackboard with all results (of all steps)
     */
    public static DataStructure run(String name, File inputText, File inputArchitectureModel, File additionalConfigs) throws IOException {
        return runAndSave(name, inputText, inputArchitectureModel, null, additionalConfigs, null);
    }

    /**
     * Run the approach with the given parameters and save the output to the file system.
     *
     * @param name                   Name of the run
     * @param inputText              File of the input text.
     * @param inputArchitectureModel File of the input model (PCM)
     * @param inputCodeModel         File of the input model (Java Code JSON)
     * @param additionalConfigsFile  File with the additional or overwriting config parameters that should be used
     * @param outputDir              File that represents the output directory where the results should be written to
     * @return the {@link DataStructure} that contains the blackboard with all results (of all steps)
     */
    public static DataStructure runAndSave(String name, File inputText, File inputArchitectureModel, File inputCodeModel, File additionalConfigsFile,
            File outputDir) throws IOException {

        logger.info("Loading additional configs ..");
        var additionalConfigs = loadAdditionalConfigs(additionalConfigsFile);

        logger.info("Starting {}", name);
        var startTime = System.currentTimeMillis();

        ArDoCo arDoCo = new ArDoCo();
        definePipeline(inputText, inputArchitectureModel, inputCodeModel, arDoCo, additionalConfigs);
        arDoCo.run();

        // save step
        var duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
        saveOutput(name, outputDir, arDoCo, duration);

        logger.info("Finished in {}.{}s.", duration.getSeconds(), duration.toMillisPart());

        arDoCo.setDataStructure(new DataStructure(arDoCo.getDataRepository()));
        return arDoCo.getDataStructure();
    }

    private static void definePipeline(File inputText, File inputArchitectureModel, File inputCodeModel, ArDoCo arDoCo, Map<String, String> additionalConfigs)
            throws IOException {
        var dataRepository = arDoCo.getDataRepository();

        arDoCo.addPipelineStep(getTextProvider(inputText, additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(getPcmModelProvider(inputArchitectureModel, dataRepository));
        if (inputCodeModel != null) {
            arDoCo.addPipelineStep(getJavaModelProvider(inputCodeModel, dataRepository));
        }
        arDoCo.addPipelineStep(getTextExtraction(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(getRecommendationGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(getConnectionGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(getInconsistencyChecker(additionalConfigs, dataRepository));
    }

    private static void saveOutput(String name, File outputDir, ArDoCo arDoCo, Duration duration) {
        if (outputDir == null) {
            return;
        }
        var dataRepository = arDoCo.getDataRepository();
        var modelStatesData = getModelStatesData(dataRepository);
        logger.info("Writing output.");
        for (String modelId : modelStatesData.modelIds()) {
            // write model states
            ModelExtractionState modelState = modelStatesData.getModelState(modelId);
            var metaModel = modelState.getMetamodel();
            var modelStateFile = Path.of(outputDir.getAbsolutePath(), name + "-instances-" + metaModel + ".csv").toFile();
            FilePrinter.writeModelInstancesInCsvFile(modelStateFile, modelState, name);

            // write results
            arDoCo.printResultsInFiles(outputDir, modelId, name, dataRepository, duration);
        }
    }

    public static InconsistencyChecker getInconsistencyChecker(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var inconsistencyChecker = new InconsistencyChecker(dataRepository);
        inconsistencyChecker.applyConfiguration(additionalConfigs);
        return inconsistencyChecker;
    }

    public static ConnectionGenerator getConnectionGenerator(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var connectionGenerator = new ConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(additionalConfigs);
        return connectionGenerator;
    }

    public static RecommendationGenerator getRecommendationGenerator(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var recommendationGenerator = new RecommendationGenerator(dataRepository);
        recommendationGenerator.applyConfiguration(additionalConfigs);
        return recommendationGenerator;
    }

    public static TextExtraction getTextExtraction(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var textExtractor = new TextExtraction(dataRepository);
        textExtractor.applyConfiguration(additionalConfigs);
        return textExtractor;
    }

    public static ModelProvider getJavaModelProvider(File inputCodeModel, DataRepository dataRepository) throws IOException {
        ModelConnector javaModel = new JavaJsonModelConnector(inputCodeModel);
        var javaModelProvider = new ModelProvider(dataRepository, javaModel);
        return javaModelProvider;
    }

    public static ModelProvider getPcmModelProvider(File inputArchitectureModel, DataRepository dataRepository) throws IOException {
        ModelConnector pcmModel = new PcmXMLModelConnector(inputArchitectureModel);
        var pcmModelProvider = new ModelProvider(dataRepository, pcmModel);
        return pcmModelProvider;
    }

    public static CoreNLPProvider getTextProvider(File inputText, Map<String, String> additionalConfigs, DataRepository dataRepository)
            throws FileNotFoundException {
        var textProvider = new CoreNLPProvider(dataRepository, new FileInputStream(inputText));
        textProvider.applyConfiguration(additionalConfigs);
        return textProvider;
    }

    private static Map<String, String> loadAdditionalConfigs(File additionalConfigsFile) {
        Map<String, String> additionalConfigs = new HashMap<>();
        if (additionalConfigsFile != null && additionalConfigsFile.exists()) {
            try (var scanner = new Scanner(additionalConfigsFile, StandardCharsets.UTF_8.name())) {
                while (scanner.hasNextLine()) {
                    var line = scanner.nextLine();
                    if (line == null || line.isBlank()) {
                        continue;
                    }
                    var values = line.split(KEY_VALUE_CONNECTOR, 2);
                    if (values.length != 2) {
                        logger.error("Found config line \"{}\". Layout has to be: 'KEY" + KEY_VALUE_CONNECTOR + "VALUE', e.g., 'SimpleClassName"
                                + CLASS_ATTRIBUTE_CONNECTOR + "AttributeName" + KEY_VALUE_CONNECTOR + "42", line);
                    } else {
                        additionalConfigs.put(values[0], values[1]);
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return additionalConfigs;
    }

    private void printResultsInFiles(File outputDir, String modelId, String name, DataRepository data, Duration duration) {
        var textState = getTextState(data);
        var modelState = getModelStatesData(data).getModelState(modelId);
        var metaModel = modelState.getMetamodel();
        var recommendationState = getRecommendationStates(data).getRecommendationState(metaModel);
        var connectionState = getConnectionStates(data).getConnectionState(metaModel);
        var inconsistencyStates = getInconsistencyStates(data);
        var inconsistencyState = inconsistencyStates.getInconsistencyState(Metamodel.ARCHITECTURE);

        FilePrinter.writeNounMappingsInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "_noun_mappings.csv").toFile(), //
                textState);

        FilePrinter.writeTraceLinksInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "_trace_links.csv").toFile(), //
                connectionState);

        FilePrinter.writeStatesToFile(Path.of(outputDir.getAbsolutePath(), name + "_states.csv").toFile(), //
                modelState, textState, recommendationState, connectionState, duration);

        FilePrinter.writeInconsistenciesToFile(Path.of(outputDir.getAbsolutePath(), name + "_inconsistencies.csv").toFile(), inconsistencyState);
    }

}
