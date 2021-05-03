package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGeneratorConfig;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents_extractors.GenericConnectionAnalyzerSolverConfig;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IModule;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.exception.InconsistentModelException;
import edu.kit.kastel.mcse.ardoco.core.model.pcm.PcmOntologyModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.provider.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.pipeline.helpers.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGeneratorConfig;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors.GenericRecommendationConfig;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.textextractor.TextExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextractor.TextExtractorConfig;
import edu.kit.kastel.mcse.ardoco.core.textextractor.agents_extractors.GenericTextConfig;

public class Pipeline {

    private Pipeline() {
        throw new IllegalAccessError();
    }

    private static final Logger logger = LogManager.getLogger(Pipeline.class);

    private static final String CMD_HELP = "h";
    private static final String CMD_NAME = "n";
    private static final String CMD_MODEL = "m";
    private static final String CMD_TEXT = "t";
    private static final String CMD_CONF = "c";
    private static final String CMD_OUT_DIR = "o";

    public static void main(String[] args) {
        // Parameters:
        // -h : Help
        // -n : Name of the Run
        // -m : Model Path
        // -t : Text Path
        // -c : Configuration Path (only property overrides)
        // -o : Output folder

        CommandLine cmd = null;
        try {
            cmd = parseCommandLine(args);
        } catch (IllegalArgumentException | ParseException e) {
            logger.error(e.getMessage());
            printUsage();
            System.exit(1);
        }

        if (cmd.hasOption(CMD_HELP)) {
            printUsage();
            return;
        }

        File inputText = null;
        File inputModel = null;
        File additionalConfigs = null;
        File outputDir = null;

        try {
            inputText = ensureFile(cmd.getOptionValue(CMD_TEXT), false);
            inputModel = ensureFile(cmd.getOptionValue(CMD_MODEL), false);
            if (cmd.hasOption(CMD_CONF)) {
                additionalConfigs = ensureFile(cmd.getOptionValue(CMD_CONF), false);
            }

            outputDir = ensureDir(cmd.getOptionValue(CMD_OUT_DIR), true);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            System.exit(2);
        }

        String name = cmd.getOptionValue(CMD_NAME);

        if (!name.matches("[A-Za-z0-9_]+")) {
            logger.error("Name does not match [A-Za-z0-9_]+");
            System.exit(1);
        }

        run(name, inputText, inputModel, additionalConfigs, outputDir);
    }

    private static void printUsage() {
        System.err.println(
                """
                        Usage: java -jar ardoco-core-pipeline.jar

                        -n NAME_OF_THE_PROJECT (will be stored in the results)
                        -m PATH_TO_THE_PCM_MODEL_AS_OWL (use Ecore2OWL to obtain PCM models as ontology)
                        -t PATH_TO_PLAIN_TEXT
                        -o PATH_TO_OUTPUT_FOLDER

                        Optional Parameters:

                        -c CONFIG_FILE (the config file can override any default configuration using the standard property syntax (see config files in src/main/resources)

                        """);
    }

    private static void run(String name, File inputText, File inputModel, File additionalConfigs, File outputDir) {
        long startTime = System.currentTimeMillis();

        IText annotatedText = null;

        try {
            ITextConnector textConnector = new ParseProvider(new FileInputStream(inputText));
            annotatedText = textConnector.getAnnotatedText();
        } catch (IOException | LunaRunException | LunaInitException e) {
            logger.error(e.getMessage(), e);
            System.exit(1);
        }

        IModelConnector pcmModel = new PcmOntologyModelConnector(inputModel.getAbsolutePath());
        FilePrinter.writeModelInstancesInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "-instances.csv").toFile(), runModelExtractor(pcmModel), name);

        AgentDatastructure data = new AgentDatastructure(annotatedText, null, runModelExtractor(pcmModel), null, null);
        data.overwrite(runTextExtractor(data, additionalConfigs));
        data.overwrite(runRecommendationGenerator(data, additionalConfigs));
        data.overwrite(runConnectionGenerator(data, additionalConfigs));

        Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
        printResultsInFiles(outputDir, name, data, duration);
    }

    private static void printResultsInFiles(File outputDir, String name, AgentDatastructure data, Duration duration) {

        FilePrinter.writeNounMappingsInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "_noun_mappings.csv").toFile(), //
                data.getTextState());

        FilePrinter.writeTraceLinksInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "_trace_links.csv").toFile(), //
                data.getConnectionState());

        FilePrinter.writeStatesToFile(Path.of(outputDir.getAbsolutePath(), name + "_states.csv").toFile(), //
                data.getModelState(), data.getTextState(), data.getRecommendationState(), data.getConnectionState(), duration);
    }

    private static IModelState runModelExtractor(IModelConnector modelConnector) throws InconsistentModelException {
        IModule<IModelState> hardCodedModelExtractor = new ModelProvider(modelConnector);
        hardCodedModelExtractor.exec();
        return hardCodedModelExtractor.getState();
    }

    private static AgentDatastructure runTextExtractor(AgentDatastructure data, File additionalConfigs) {
        IModule<AgentDatastructure> textModule = new TextExtractor(data);
        if (additionalConfigs != null) {
            Map<String, String> configs = new HashMap<>();
            Configuration.mergeConfigToMap(configs, TextExtractorConfig.DEFAULT_CONFIG);
            Configuration.mergeConfigToMap(configs, GenericTextConfig.DEFAULT_CONFIG);
            Configuration.overrideConfigInMap(configs, additionalConfigs);
            textModule = textModule.create(data, configs);
        }

        textModule.exec();
        return textModule.getState();
    }

    private static AgentDatastructure runRecommendationGenerator(AgentDatastructure data, File additionalConfigs) {
        IModule<AgentDatastructure> recommendationModule = new RecommendationGenerator(data);

        if (additionalConfigs != null) {
            Map<String, String> configs = new HashMap<>();
            Configuration.mergeConfigToMap(configs, RecommendationGeneratorConfig.DEFAULT_CONFIG);
            Configuration.mergeConfigToMap(configs, GenericRecommendationConfig.DEFAULT_CONFIG);
            Configuration.overrideConfigInMap(configs, additionalConfigs);
            recommendationModule = recommendationModule.create(data, configs);
        }

        recommendationModule.exec();
        return recommendationModule.getState();
    }

    private static AgentDatastructure runConnectionGenerator(AgentDatastructure data, File additionalConfigs) {
        IModule<AgentDatastructure> connectionGenerator = new ConnectionGenerator(data);

        if (additionalConfigs != null) {
            Map<String, String> configs = new HashMap<>();
            Configuration.mergeConfigToMap(configs, ConnectionGeneratorConfig.DEFAULT_CONFIG);
            Configuration.mergeConfigToMap(configs, GenericConnectionAnalyzerSolverConfig.DEFAULT_CONFIG);
            Configuration.overrideConfigInMap(configs, additionalConfigs);
            connectionGenerator = connectionGenerator.create(data, configs);
        }

        connectionGenerator.exec();
        return connectionGenerator.getState();
    }

    /**
     * Ensure that a file exists (or create if allowed by parameter).
     *
     * @param path   the path to the file
     * @param create indicates whether creation is allowed
     * @return the file
     * @throws IOException if something went wrong
     */
    private static File ensureFile(String path, boolean create) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        if (create && file.createNewFile()) {
            return file;
        }
        // File not available
        throw new IOException("The specified file does not exist and/or could not be created: " + path);
    }

    /**
     * Ensure that a directory exists (or create if allowed by parameter).
     *
     * @param path   the path to the file
     * @param create indicates whether creation is allowed
     * @return the file
     * @throws IOException if something went wrong
     */
    private static File ensureDir(String path, boolean create) throws IOException {
        File file = new File(path);
        if (file.isDirectory() && file.exists()) {
            return file;
        }
        if (create) {
            file.mkdirs();
            return file;
        }

        // File not available
        throw new IOException("The specified directory does not exist: " + path);
    }

    private static CommandLine parseCommandLine(String[] args) throws ParseException {
        Options options = new Options();
        Option opt;

        // Define Options ..
        opt = new Option(CMD_HELP, "help", false, "show help");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(CMD_NAME, "name", true, "name of the run");
        opt.setRequired(true);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option(CMD_MODEL, "model", true, "path to the owl model");
        opt.setRequired(true);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option(CMD_TEXT, "text", true, "path to the text file");
        opt.setRequired(true);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option(CMD_CONF, "conf", true, "path to the additional config file");
        opt.setRequired(false);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option(CMD_OUT_DIR, "out", true, "path to the output directory");
        opt.setRequired(true);
        opt.setType(String.class);
        options.addOption(opt);

        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);

    }
}
