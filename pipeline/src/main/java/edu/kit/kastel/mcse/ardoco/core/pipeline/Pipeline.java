/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.io.File;
import java.io.FileInputStream;
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
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStatesData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.stage.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.JavaJsonModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.helpers.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

/**
 * The Pipeline defines the execution of the agents.
 */
public final class Pipeline extends edu.kit.kastel.informalin.pipeline.Pipeline {

    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

    public Pipeline(String id, DataRepository dataRepository) {
        super(id, dataRepository);
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
        Pipeline pipeline = new Pipeline("ArDoCo", new DataRepository());
        logger.info("Loading additional configs ..");
        var additionalConfigs = loadAdditionalConfigs(additionalConfigsFile);

        logger.info("Starting {}", name);
        var startTime = System.currentTimeMillis();
        var dataRepository = pipeline.getDataRepository();

        // Preprocess: text provider
        var textProvider = new CoreNLPProvider(dataRepository, new FileInputStream(inputText));
        pipeline.addPipelineStep(textProvider);

        // Preprocess: model connectors
        pipeline.addPcmModelProviderToPipeline(inputArchitectureModel, additionalConfigs);

        if (inputCodeModel != null) {
            pipeline.addJavaModelProviderToPipeline(inputCodeModel, additionalConfigs);
        }

        // TODO remove
        var annotatedText = pipeline.getAnnotatedText(inputText);
        if (annotatedText == null) {
            logger.info("Could not preprocess or receive annotated text. Exiting.");
            return null;
        }
        var models = pipeline.getDataRepository().getData("ModelStatesData", ModelStatesData.class).orElseThrow();
        var data = new DataStructure(annotatedText, models);

        if (outputDir != null) {
            for (String modelId : data.getModelIds()) {
                var modelStateFile = Path.of(outputDir.getAbsolutePath(), name + "-instances-" + data.getModelState(modelId).getMetamodel().toString() + ".csv")
                        .toFile();
                FilePrinter.writeModelInstancesInCsvFile(modelStateFile, data.getModelState(modelId), name);
            }
        }

        // text extractor
        pipeline.runTextExtractor(data, additionalConfigs);

        // recommendation generator
        pipeline.runRecommendationGenerator(data, additionalConfigs);

        // connection generator
        pipeline.runConnectionGenerator(data, additionalConfigs);

        // inconsistency checker
        pipeline.runInconsistencyChecker(data, additionalConfigs);

        // save step
        var duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
        if (outputDir != null) {
            logger.info("Writing output.");
            for (String modelId : data.getModelIds()) {
                pipeline.printResultsInFiles(outputDir, modelId, name, data, duration);
            }
        }

        pipeline.run();

        logger.info("Finished in {}.{}s.", duration.getSeconds(), duration.toMillisPart());

        return data;
    }

    private void addJavaModelProviderToPipeline(File inputCodeModel, Map<String, String> additionalConfigs) throws IOException {
        IModelConnector javaModel = new JavaJsonModelConnector(inputCodeModel);
        var javaModelProvider = new ModelProvider(this.getDataRepository(), javaModel);
        javaModelProvider.setAdditionalSettings(additionalConfigs);
        this.addPipelineStep(javaModelProvider);
    }

    private void addPcmModelProviderToPipeline(File inputArchitectureModel, Map<String, String> additionalConfigs) throws IOException {
        IModelConnector pcmModel = new PcmXMLModelConnector(inputArchitectureModel);
        var pcmModelProvider = new ModelProvider(this.getDataRepository(), pcmModel);
        pcmModelProvider.setAdditionalSettings(additionalConfigs);
        this.addPipelineStep(pcmModelProvider);
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

    private static void logTiming(long startTime, String step) {
        var duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
        logger.info("Finished step {} in {}.{}s.", step, duration.getSeconds(), duration.toMillisPart());
    }

    private IText getAnnotatedText(File inputText) {
        try {
            ITextConnector textConnector = new CoreNLPProvider(getDataRepository(), new FileInputStream(inputText));
            return textConnector.getAnnotatedText();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

    }

    private void printResultsInFiles(File outputDir, String modelId, String name, DataStructure data, Duration duration) {

        FilePrinter.writeNounMappingsInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "_noun_mappings.csv").toFile(), //
                data.getTextState());

        FilePrinter.writeTraceLinksInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "_trace_links.csv").toFile(), //
                data.getConnectionState(modelId));

        FilePrinter.writeStatesToFile(Path.of(outputDir.getAbsolutePath(), name + "_states.csv").toFile(), //
                data.getModelState(modelId), data.getTextState(), data.getRecommendationState(data.getModelState(modelId).getMetamodel()),
                data.getConnectionState(modelId), duration);

        FilePrinter.writeInconsistenciesToFile(Path.of(outputDir.getAbsolutePath(), name + "_inconsistencies.csv").toFile(),
                data.getInconsistencyState(modelId));
    }

    private void runTextExtractor(DataStructure data, Map<String, String> additionalConfigs) {
        IExecutionStage textModule = new TextExtraction();
        textModule.execute(data, additionalConfigs);
    }

    private void runRecommendationGenerator(DataStructure data, Map<String, String> additionalConfigs) {
        IExecutionStage recommendationModule = new RecommendationGenerator();
        recommendationModule.execute(data, additionalConfigs);
    }

    private void runConnectionGenerator(DataStructure data, Map<String, String> additionalConfigs) {
        IExecutionStage connectionGenerator = new ConnectionGenerator();
        connectionGenerator.execute(data, additionalConfigs);
    }

    private void runInconsistencyChecker(DataStructure data, Map<String, String> additionalConfigs) {
        IExecutionStage inconsistencyChecker = new InconsistencyChecker();
        inconsistencyChecker.execute(data, additionalConfigs);
    }

}
