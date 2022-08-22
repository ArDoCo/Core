/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple CLI for execution of the agents.
 */
public final class ArDoCoCLI {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoCLI.class);

    private static final String CMD_HELP = "h";
    private static final String CMD_NAME = "n";
    private static final String CMD_MODEL_ARCHITECTURE = "ma";
    private static final String CMD_MODEL_ARCHITECTURE_TYPE = "mt";
    private static final String CMD_MODEL_CODE = "mc";
    private static final String CMD_TEXT = "t";
    private static final String CMD_CONF = "c";
    private static final String CMD_OUT_DIR = "o";

    private static Options options;

    private ArDoCoCLI() {
        throw new IllegalAccessError();
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        // Parameters:
        // -h : Help
        // -n : Name of the Run
        // -ma : Model Path (Architecture)
        // -mc : Model Path (Java Code)
        // -mt : Model Type (PCM or UML)
        // -t : Path to Text File
        // -c : Configuration Path (only property overrides)
        // -o : Output folder

        ArDoCoRunner arDoCoRunner = parseCommandLineAndBuildArDoCoRunner(args);
        if (arDoCoRunner == null)
            return;
        arDoCoRunner.runArDoCo();
    }

    static ArDoCoRunner parseCommandLineAndBuildArDoCoRunner(String[] args) {
        CommandLine cmd;
        try {
            cmd = parseCommandLine(args);
        } catch (IllegalArgumentException | ParseException e) {
            logger.error(e.getMessage());
            printUsage();
            return null;
        }

        if (cmd.hasOption(CMD_HELP)) {
            printUsage();
            return null;
        }

        File inputText;
        File inputModelArchitecture;
        ArchitectureModelType inputArchitectureModelType = ArchitectureModelType.PCM;
        File inputModelCode;
        File additionalConfigs = null;
        File outputDir;

        if (!cmd.hasOption(CMD_TEXT)) {
            printUsage();
            return null;
        }

        if (cmd.hasOption(CMD_MODEL_ARCHITECTURE_TYPE)) {
            inputArchitectureModelType = ArchitectureModelType.valueOf(cmd.getOptionValue(CMD_MODEL_ARCHITECTURE_TYPE));
        }

        try {
            inputText = ensureFile(cmd.getOptionValue(CMD_TEXT));
            inputModelArchitecture = ensureFile(cmd.getOptionValue(CMD_MODEL_ARCHITECTURE));
            inputModelCode = cmd.hasOption(CMD_MODEL_CODE) ? ensureFile(cmd.getOptionValue(CMD_MODEL_ARCHITECTURE)) : null;
            if (cmd.hasOption(CMD_CONF)) {
                additionalConfigs = ensureFile(cmd.getOptionValue(CMD_CONF));
            }

            outputDir = ensureDir(cmd.getOptionValue(CMD_OUT_DIR));
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }

        var name = cmd.getOptionValue(CMD_NAME);

        if (!name.matches("\\w+")) {
            logger.error("Name does not match [A-Za-z0-9_]+");
            return null;
        }

        return new ArDoCoRunner(inputText, inputModelArchitecture, inputArchitectureModelType, inputModelCode, additionalConfigs, outputDir, name);
    }

    private static void printUsage() {
        var formatter = new HelpFormatter();
        formatter.printHelp("java -jar ardoco-core-pipeline.jar", options);
    }

    /**
     * Ensure that a file exists.
     *
     * @param path the path to the file
     * @return the file
     * @throws IOException if something went wrong
     */
    private static File ensureFile(String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IOException("The specified file does not exist and/or could not be created: " + path);
        }
        var file = new File(path);
        if (file.exists()) {
            return file;
        }
        // File not available
        throw new IOException("The specified file does not exist and/or could not be created: " + path);
    }

    /**
     * Ensure that a directory exists (or create ).
     *
     * @param path the path to the file
     * @return the file
     */
    private static File ensureDir(String path) {
        var file = new File(path);
        if (file.isDirectory() && file.exists()) {
            return file;
        }
        file.mkdirs();
        return file;
    }

    private static CommandLine parseCommandLine(String[] args) throws ParseException {
        options = new Options();
        Option opt;

        // Define Options ..
        opt = new Option(CMD_HELP, "help", false, "show this message");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(CMD_NAME, "name", true, "name of the run");
        opt.setRequired(true);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option(CMD_MODEL_ARCHITECTURE, "model-architecture", true, "path to the architecture model");
        opt.setRequired(true);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option(CMD_MODEL_ARCHITECTURE_TYPE, "archtype", true, "type of the architecture model (PCM or UML)");
        opt.setRequired(false);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option(CMD_MODEL_CODE, "model-code", true, "path to the java code model");
        opt.setRequired(false);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option(CMD_TEXT, "text", true, "path to the text file");
        opt.setRequired(false);
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
