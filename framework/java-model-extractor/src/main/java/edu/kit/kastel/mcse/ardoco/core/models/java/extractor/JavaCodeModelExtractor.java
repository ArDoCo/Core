/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.java.extractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.JsonUtils;
import edu.kit.kastel.mcse.ardoco.core.models.java.JavaProject;
import edu.kit.kastel.mcse.ardoco.core.models.java.extractor.visitors.JavaFileVisitor;

/**
 * This class provides a CLI to extract a Java Code Model from Java Projects.
 */
public class JavaCodeModelExtractor {
    private static final Logger logger = LoggerFactory.getLogger(JavaCodeModelExtractor.class);

    private static final String CMD_HELP = "h";
    private static final String CMD_IN_DIR = "i";
    private static final String CMD_OUT_DIR = "o";
    private static Options options;

    private JavaCodeModelExtractor() {
        throw new IllegalAccessError();
    }

    public static void main(String[] args) {
        CommandLine cmd;
        try {
            cmd = parseCommandLine(args);
        } catch (IllegalArgumentException | ParseException e) {
            logger.error(e.getMessage());
            printUsage();
            return;
        }

        if (cmd.hasOption(CMD_HELP)) {
            printUsage();
            return;
        }

        Path inputDir;
        File outputFile;
        inputDir = ensureDir(cmd.getOptionValue(CMD_IN_DIR));
        outputFile = new File(cmd.getOptionValue(CMD_OUT_DIR));
        runExtraction(inputDir, outputFile);
    }

    private static void runExtraction(Path startingDir, File outputFile) {
        logger.info("Start extracting \"{}\".", startingDir);
        var javaFileVisitor = new JavaFileVisitor();
        // walk all files and run the JavaFileVisitor
        try {
            Files.walkFileTree(startingDir, javaFileVisitor);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e.getCause());
        }
        // afterwards, process information and save them
        processAndSaveInformation(javaFileVisitor.getProject(), outputFile);
    }

    private static void processAndSaveInformation(JavaProject javaProject, File outputFile) {
        // process
        // no process for now
        if (logger.isInfoEnabled()) {
            var numClasses = javaProject.getClassesAndInterfaces().size();
            logger.info("Extraction finished with {} extracted classes and interfaces.", numClasses);
        }

        // finally, save the information
        saveToJSON(javaProject, outputFile);
    }

    private static void saveToJSON(JavaProject javaProject, File outputFile) {
        try {
            JsonUtils.createObjectMapper().writeValue(outputFile, javaProject);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void printUsage() {
        var formatter = new HelpFormatter();
        formatter.printHelp("JavaCodeModelExtractor.jar", options);
    }

    private static CommandLine parseCommandLine(String[] args) throws ParseException {
        options = new Options();
        Option opt;

        // Define Options ..
        opt = new Option(CMD_HELP, "help", false, "print this message");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(CMD_IN_DIR, "in", true, "path to the input directory");
        opt.setRequired(true);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option(CMD_OUT_DIR, "out", true, "path to the output file that should be used for saving");
        opt.setRequired(true);
        opt.setType(String.class);
        options.addOption(opt);

        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    /**
     * Ensure that a directory exists (or create).
     *
     * @param path the path to the file
     * @return the file
     */
    private static Path ensureDir(String path) {
        var file = new File(path);
        if (file.isDirectory() && file.exists()) {
            return Paths.get(file.toURI());
        }

        file.mkdirs();
        return Paths.get(file.toURI());

    }

}
