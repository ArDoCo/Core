/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import static edu.kit.kastel.mcse.ardoco.core.api.common.AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR;
import static edu.kit.kastel.mcse.ardoco.core.api.common.AbstractConfigurable.KEY_VALUE_CONNECTOR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.stage.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.java.JavaJsonModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.pcm.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.provider.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.pipeline.helpers.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.json.JsonTextProvider;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

/**
 * The Pipeline defines the execution of the agents.
 */
public final class Pipeline {

    private Pipeline() {
        throw new IllegalAccessError();
    }

    private static final Logger logger = LogManager.getLogger(Pipeline.class);

    /**
     * Run the approach with the given parameters and save the output to the file system.
     *
     * @param name                   Name of the run
     * @param inputText              File of the input text.
     * @param preprocessedText       indicator whether this file is already preprocessed text.
     * @param inputArchitectureModel File of the input model (PCM)
     * @return the {@link DataStructure} that contains the blackboard with all results (of all steps)
     */
    public static DataStructure run(String name, File inputText, boolean preprocessedText, File inputArchitectureModel, File additionalConfigs)
            throws ReflectiveOperationException, IOException, ParserConfigurationException, SAXException {
        return runAndSave(name, inputText, preprocessedText, inputArchitectureModel, null, additionalConfigs, null);
    }

    /**
     * Run the approach with the given parameters and save the output to the file system.
     *
     * @param name                   Name of the run
     * @param inputText              File of the input text.
     * @param preprocessedText       indicator whether this file is already preprocessed text.
     * @param inputArchitectureModel File of the input model (PCM)
     * @param inputCodeModel         File of the input model (Java Code JSON)
     * @param additionalConfigsFile  File with the additional or overwriting config parameters that should be used
     * @param outputDir              File that represents the output directory where the results should be written to
     * @return the {@link DataStructure} that contains the blackboard with all results (of all steps)
     */
    public static DataStructure runAndSave(String name, File inputText, boolean preprocessedText, File inputArchitectureModel, File inputCodeModel,
            File additionalConfigsFile, File outputDir) throws IOException, ReflectiveOperationException, ParserConfigurationException, SAXException {
        logger.info("Loading additional configs ..");
        Map<String, String> additionalConfigs = loadAdditionalConfigs(additionalConfigsFile);

        logger.info("Starting {}", name);
        var startTime = System.currentTimeMillis();

        logger.info("Preparing and preprocessing text input.");
        var annotatedText = getAnnotatedText(inputText, preprocessedText);
        if (annotatedText == null) {
            logger.info("Could not preprocess or receive annotated text. Exiting.");
            return null;
        }

        logger.info("Starting process to generate Trace Links");
        var prevStartTime = System.currentTimeMillis();
        Map<String, IModelState> models = new HashMap<>();
        IModelConnector pcmModel = new PcmXMLModelConnector(inputArchitectureModel);
        models.put(pcmModel.getModelId(), runModelExtractor(pcmModel, additionalConfigs));

        if (inputCodeModel != null) {
            IModelConnector javaModel = new JavaJsonModelConnector(inputCodeModel);
            var codeModelState = runModelExtractor(javaModel, additionalConfigs);
            models.put(javaModel.getModelId(), codeModelState);
        }
        var data = new DataStructure(annotatedText, models);

        if (outputDir != null) {
            for (String modelId : data.getModelIds()) {
                var modelStateFile = Path.of(outputDir.getAbsolutePath(), name + "-instances-" + data.getModelState(modelId).getMetamodel().toString() + ".csv")
                        .toFile();
                FilePrinter.writeModelInstancesInCsvFile(modelStateFile, data.getModelState(modelId), name);
            }
        }
        logTiming(prevStartTime, "Model-Extractor");

        prevStartTime = System.currentTimeMillis();
        runTextExtractor(data, additionalConfigs);
        logTiming(prevStartTime, "Text-Extractor");

        prevStartTime = System.currentTimeMillis();
        runRecommendationGenerator(data, additionalConfigs);
        logTiming(prevStartTime, "Recommendation-Generator");

        prevStartTime = System.currentTimeMillis();
        runConnectionGenerator(data, additionalConfigs);
        logTiming(prevStartTime, "Connection-Generator");

        prevStartTime = System.currentTimeMillis();
        runInconsistencyChecker(data, additionalConfigs);
        logTiming(prevStartTime, "Inconsistency-Checker");

        var duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
        logger.info("Finished in {}.{}s.", duration.getSeconds(), duration.toMillisPart());

        if (outputDir != null) {
            logger.info("Writing output.");
            prevStartTime = System.currentTimeMillis();

            for (String modelId : data.getModelIds()) {
                printResultsInFiles(outputDir, modelId, name, data, duration);
            }
            logTiming(prevStartTime, "Saving");
        }

        return data;
    }

    private static Map<String, String> loadAdditionalConfigs(File additionalConfigsFile) {
        Map<String, String> additionalConfigs = new HashMap<>();
        if (additionalConfigsFile != null && additionalConfigsFile.exists()) {
            try (Scanner scanner = new Scanner(additionalConfigsFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line == null || line.isBlank())
                        continue;
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

    private static IText getAnnotatedText(File inputText, boolean providedAnalyzedText) {
        if (providedAnalyzedText) {
            try {
                return JsonTextProvider.loadFromFile(inputText).getAnnotatedText();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }

        try {
            ITextConnector textConnector = new ParseProvider(new FileInputStream(inputText));
            return textConnector.getAnnotatedText();
        } catch (IOException | LunaRunException | LunaInitException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

    }

    private static void printResultsInFiles(File outputDir, String modelId, String name, DataStructure data, Duration duration) {

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

    private static IModelState runModelExtractor(IModelConnector modelConnector, Map<String, String> additionalConfigs) {
        ModelProvider modelExtractor = new ModelProvider(modelConnector);
        return modelExtractor.execute(additionalConfigs);
    }

    private static void runTextExtractor(DataStructure data, Map<String, String> additionalConfigs) {
        IExecutionStage textModule = new TextExtraction();
        textModule.execute(data, additionalConfigs);
    }

    private static void runRecommendationGenerator(DataStructure data, Map<String, String> additionalConfigs) {
        IExecutionStage recommendationModule = new RecommendationGenerator();
        recommendationModule.execute(data, additionalConfigs);
    }

    private static void runConnectionGenerator(DataStructure data, Map<String, String> additionalConfigs) {
        IExecutionStage connectionGenerator = new ConnectionGenerator();
        connectionGenerator.execute(data, additionalConfigs);
    }

    private static void runInconsistencyChecker(DataStructure data, Map<String, String> additionalConfigs) {
        IExecutionStage inconsistencyChecker = new InconsistencyChecker();
        inconsistencyChecker.execute(data, additionalConfigs);
    }

}
