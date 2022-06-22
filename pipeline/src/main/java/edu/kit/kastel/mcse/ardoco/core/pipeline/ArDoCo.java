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

    private static final Logger logger = LoggerFactory.getLogger(ArDoCo.class);

    public ArDoCo() {
        super("ArDoCo", new DataRepository());
    }

    /**
     * Run the approach with the given parameters and save the output to the file system.
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

        return new DataStructure(arDoCo.getDataRepository());
    }

    private static void definePipeline(File inputText, File inputArchitectureModel, File inputCodeModel, ArDoCo arDoCo, Map<String, String> additionalConfigs)
            throws IOException {
        addTextProvider(inputText, arDoCo, additionalConfigs);
        addModelProviders(inputArchitectureModel, inputCodeModel, arDoCo);
        addTextExtractor(arDoCo, additionalConfigs);
        addRecommendationGenerator(arDoCo, additionalConfigs);
        addConnectionGenerator(arDoCo, additionalConfigs);
        addInconsistencyChecker(arDoCo, additionalConfigs);
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

    private static void addInconsistencyChecker(ArDoCo arDoCo, Map<String, String> additionalConfigs) {
        var dataRepository = arDoCo.getDataRepository();
        var inconsistencyChecker = new InconsistencyChecker(dataRepository);
        inconsistencyChecker.applyConfiguration(additionalConfigs);
        arDoCo.addPipelineStep(inconsistencyChecker);
    }

    private static void addConnectionGenerator(ArDoCo arDoCo, Map<String, String> additionalConfigs) {
        var dataRepository = arDoCo.getDataRepository();
        var connectionGenerator = new ConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(additionalConfigs);
        arDoCo.addPipelineStep(connectionGenerator);
    }

    private static void addRecommendationGenerator(ArDoCo arDoCo, Map<String, String> additionalConfigs) {
        var dataRepository = arDoCo.getDataRepository();
        var recommendationGenerator = new RecommendationGenerator(dataRepository);
        recommendationGenerator.applyConfiguration(additionalConfigs);
        arDoCo.addPipelineStep(recommendationGenerator);
    }

    private static void addTextExtractor(ArDoCo arDoCo, Map<String, String> additionalConfigs) {
        var dataRepository = arDoCo.getDataRepository();
        var textExtractor = new TextExtraction(dataRepository);
        textExtractor.applyConfiguration(additionalConfigs);
        arDoCo.addPipelineStep(textExtractor);
    }

    private static void addModelProviders(File inputArchitectureModel, File inputCodeModel, ArDoCo arDoCo) throws IOException {
        ModelConnector pcmModel = new PcmXMLModelConnector(inputArchitectureModel);
        DataRepository dataRepository = arDoCo.getDataRepository();
        var pcmModelProvider = new ModelProvider(dataRepository, pcmModel);
        arDoCo.addPipelineStep(pcmModelProvider);
        if (inputCodeModel != null) {
            ModelConnector javaModel = new JavaJsonModelConnector(inputCodeModel);
            var javaModelProvider = new ModelProvider(dataRepository, javaModel);
            arDoCo.addPipelineStep(javaModelProvider);
        }
    }

    private static void addTextProvider(File inputText, ArDoCo arDoCo, Map<String, String> additionalConfigs) throws FileNotFoundException {
        var dataRepository = arDoCo.getDataRepository();
        var textProvider = new CoreNLPProvider(dataRepository, new FileInputStream(inputText));
        textProvider.applyConfiguration(additionalConfigs);
        arDoCo.addPipelineStep(textProvider);
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
