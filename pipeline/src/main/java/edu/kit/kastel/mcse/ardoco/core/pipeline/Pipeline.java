/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.stage.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.java.JavaOntologyModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.pcm.PcmOntologyModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.provider.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.pipeline.helpers.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ontology.OntologyTextProvider;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * The Pipeline defines a simple CLI for execution of the agents.
 */
public final class Pipeline {

	private Pipeline() {
		throw new IllegalAccessError();
	}

	private static final Logger logger = LogManager.getLogger(Pipeline.class);

	private static final String CMD_HELP = "h";
	private static final String CMD_NAME = "n";
	private static final String CMD_MODEL = "m";
	private static final String CMD_TEXT = "t";
	private static final String CMD_PROVIDED = "p";
	private static final String CMD_CONF = "c";
	private static final String CMD_OUT_DIR = "o";
	private static final String CMD_IMPLEMENTATION = "i";

	private static Options options;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// Parameters:
		// -h : Help
		// -n : Name of the Run
		// -m : Model Path
		// -t : Text Path (either this or -p)
		// -p : Flag to make use of provided ontology to load preprocessed text
		// -c : Configuration Path (only property overrides)
		// -o : Output folder
		// -i : Model contains Code Model

		CommandLine cmd = null;
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

		File inputText = null;
		File inputModel = null;
		File additionalConfigs = null;
		File outputDir = null;

		var providedTextOntology = cmd.hasOption(CMD_PROVIDED);
		if (!providedTextOntology && !cmd.hasOption(CMD_TEXT)) {
			printUsage();
			return;
		}

		try {
			if (!providedTextOntology) {
				inputText = ensureFile(cmd.getOptionValue(CMD_TEXT), false);
			}
			inputModel = ensureFile(cmd.getOptionValue(CMD_MODEL), false);
			if (cmd.hasOption(CMD_CONF)) {
				additionalConfigs = ensureFile(cmd.getOptionValue(CMD_CONF), false);
			}

			outputDir = ensureDir(cmd.getOptionValue(CMD_OUT_DIR), true);
		} catch (IOException e) {
			logger.error(e.getMessage());
			return;
		}

		var name = cmd.getOptionValue(CMD_NAME);

		if (!name.matches("[A-Za-z0-9_]+")) {
			logger.error("Name does not match [A-Za-z0-9_]+");
			return;
		}

		var hasJavaModel = cmd.hasOption(CMD_IMPLEMENTATION);

		runAndSave(name, inputText, inputModel, additionalConfigs, outputDir, hasJavaModel);
	}

	private static void printUsage() {
		var formatter = new HelpFormatter();
		formatter.printHelp("java -jar ardoco-core-pipeline.jar", options);
	}

	/**
	 * Run the approach equally to {@link #runAndSave(String, File, File, File, File, boolean)} but without saving the
	 * output to the file system.
	 *
	 * @param name              Name of the run
	 * @param inputText         File of the input text. Can
	 * @param inputModel        File of the input model (ontology). If inputText is null, needs to contain the text.
	 * @param additionalConfigs File with the additional or overwriting config parameters that should be used
	 * @return the {@link DataStructure} that contains the blackboard with all results (of all steps)
	 */
	public static DataStructure run(String name, File inputText, File inputModel, File additionalConfigs) {
		return runAndSave(name, inputText, inputModel, additionalConfigs, null, false);
	}

	/**
	 * Run the approach equally to {@link #runAndSave(String, File, File, File, File, boolean)} but without saving the
	 * output to the file system.
	 *
	 * @param name              Name of the run
	 * @param inputText         File of the input text. Can
	 * @param inputModel        File of the input model (ontology). If inputText is null, needs to contain the text.
	 * @param additionalConfigs File with the additional or overwriting config parameters that should be used
	 * @param hasCodeModel      indicate that the model contains a code model
	 * @return the {@link DataStructure} that contains the blackboard with all results (of all steps)
	 */
	public static DataStructure run(String name, File inputText, File inputModel, File additionalConfigs, boolean hasCodeModel) {
		return runAndSave(name, inputText, inputModel, additionalConfigs, null, hasCodeModel);
	}

	/**
	 * Run the approach with the given parameters and save the output to the file system.
	 *
	 * @param name                  Name of the run
	 * @param inputText             File of the input text. Can
	 * @param inputModel            File of the input model (ontology). If inputText is null, needs to contain the text.
	 * @param additionalConfigsFile File with the additional or overwriting config parameters that should be used
	 * @param outputDir             File that represents the output directory where the results should be written to
	 * @param hasCodeModel          indicate that the model contains a code model
	 * @return the {@link DataStructure} that contains the blackboard with all results (of all steps)
	 */
	public static DataStructure runAndSave(String name, File inputText, File inputModel, File additionalConfigsFile, File outputDir, boolean hasCodeModel) {
		logger.info("Loading additional configs ..");
		Map<String, String> additionalConfigs = new HashMap<>();
		if (additionalConfigsFile != null && additionalConfigsFile.exists()) {
			try (Scanner scanner = new Scanner(additionalConfigsFile)) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (line == null || line.isBlank())
						continue;
					var values = line.split(":", 2);
					if (values.length != 2)
						continue;
					additionalConfigs.put(values[0], values[1]);
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		logger.info("Starting {}", name);
		var startTime = System.currentTimeMillis();

		var ontoConnector = new OntologyConnector(inputModel.getAbsolutePath());

		logger.info("Preparing and preprocessing text input.");
		var annotatedText = getAnnotatedText(inputText, ontoConnector);
		if (annotatedText == null) {
			logger.info("Could not preprocess or receive annotated text. Exiting.");
			return null;
		}

		logger.info("Starting process to generate Trace Links");
		var prevStartTime = System.currentTimeMillis();
		Map<String, IModelState> models = new HashMap<>();
		IModelConnector pcmModel = new PcmOntologyModelConnector(ontoConnector);
		models.put(pcmModel.getModelId(), runModelExtractor(pcmModel, additionalConfigs));

		if (hasCodeModel) {
			IModelConnector javaModel = new JavaOntologyModelConnector(ontoConnector);
			var codeModelState = runModelExtractor(javaModel, additionalConfigs);
			models.put(javaModel.getModelId(), codeModelState);
		}
		var data = new DataStructure(annotatedText, models);

		if (outputDir != null) {
			for (String modelId : data.getModelIds()) {
				var modelStateFile = Path.of(outputDir.getAbsolutePath(), name + "-instances-" + data.getModelState(modelId).getMetamodel().toString() + ".csv").toFile();
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
				var ontoSaveFile = getOntologyOutputFile(outputDir, inputModel.getName());
				ontoConnector.save(ontoSaveFile);
			}
			logTiming(prevStartTime, "Saving");
		}

		return data;
	}

	private static void logTiming(long startTime, String step) {
		var duration = Duration.ofMillis(System.currentTimeMillis() - startTime);

		logger.info("Finished step {} in {}.{}s.", step, duration.getSeconds(), duration.toMillisPart());
	}

	private static IText getAnnotatedText(File inputText, OntologyConnector ontoConnector) {
		var ontologyTextProvider = OntologyTextProvider.get(ontoConnector);

		IText annotatedText = null;
		if (inputText != null) {
			try {
				ITextConnector textConnector = new ParseProvider(new FileInputStream(inputText));
				annotatedText = textConnector.getAnnotatedText();
			} catch (IOException | LunaRunException | LunaInitException e) {
				logger.error(e.getMessage(), e);
				return null;
			}

			ontologyTextProvider.removeExistingTexts();
			ontologyTextProvider.addText(annotatedText, inputText.getName());
		} else {
			try {
				annotatedText = ontologyTextProvider.getAnnotatedText();
			} catch (IllegalStateException e) {
				logger.warn(e.getMessage());
				return null;
			}

		}
		return annotatedText;
	}

	private static String getOntologyOutputFile(File outputDir, String name) {
		var outName = name.replace(".owl", "_processed.owl");
		if (!outName.endsWith(".owl")) {
			outName = outName + "_processed.owl";
		}
		var outFile = Path.of(outputDir.getAbsolutePath(), outName).toFile();
		return outFile.getAbsolutePath();
	}

	private static void printResultsInFiles(File outputDir, String modelId, String name, DataStructure data, Duration duration) {

		FilePrinter.writeNounMappingsInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "_noun_mappings.csv").toFile(), //
				data.getTextState());

		FilePrinter.writeTraceLinksInCsvFile(Path.of(outputDir.getAbsolutePath(), name + "_trace_links.csv").toFile(), //
				data.getConnectionState(modelId));

		FilePrinter.writeStatesToFile(Path.of(outputDir.getAbsolutePath(), name + "_states.csv").toFile(), //
				data.getModelState(modelId), data.getTextState(), data.getRecommendationState(data.getModelState(modelId).getMetamodel()), data.getConnectionState(modelId), duration);

		FilePrinter.writeInconsistenciesToFile(Path.of(outputDir.getAbsolutePath(), name + "_inconsistencies.csv").toFile(), data.getInconsistencyState(modelId));
	}

	private static IModelState runModelExtractor(IModelConnector modelConnector, Map<String, String> additionalConfigs) {
		ModelProvider modelExtractor = new ModelProvider(modelConnector);
		return modelExtractor.execute(additionalConfigs);
	}

	private static DataStructure runTextExtractor(DataStructure data, Map<String, String> additionalConfigs) {
		IExecutionStage textModule = new TextExtraction();
		textModule.execute(data, additionalConfigs);
		return data;
	}

	private static DataStructure runRecommendationGenerator(DataStructure data, Map<String, String> additionalConfigs) {
		IExecutionStage recommendationModule = new RecommendationGenerator();
		recommendationModule.execute(data, additionalConfigs);
		return data;
	}

	private static DataStructure runConnectionGenerator(DataStructure data, Map<String, String> additionalConfigs) {
		IExecutionStage connectionGenerator = new ConnectionGenerator();
		connectionGenerator.execute(data, additionalConfigs);
		return data;
	}

	private static DataStructure runInconsistencyChecker(DataStructure data, Map<String, String> additionalConfigs) {
		IExecutionStage inconsistencyChecker = new InconsistencyChecker();
		inconsistencyChecker.execute(data, additionalConfigs);
		return data;
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
		var file = new File(path);
		if (file.exists() || create && file.createNewFile()) {
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
		var file = new File(path);
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

		opt = new Option(CMD_MODEL, "model", true, "path to the owl model");
		opt.setRequired(true);
		opt.setType(String.class);
		options.addOption(opt);

		opt = new Option(CMD_TEXT, "text", true, "path to the text file");
		opt.setRequired(false);
		opt.setType(String.class);
		options.addOption(opt);

		opt = new Option(CMD_PROVIDED, "provided", false, "flag to show that ontology has text already provided");
		opt.setRequired(false);
		opt.setType(Boolean.class);
		options.addOption(opt);

		opt = new Option(CMD_CONF, "conf", true, "path to the additional config file");
		opt.setRequired(false);
		opt.setType(String.class);
		options.addOption(opt);

		opt = new Option(CMD_OUT_DIR, "out", true, "path to the output directory");
		opt.setRequired(true);
		opt.setType(String.class);
		options.addOption(opt);

		opt = new Option(CMD_IMPLEMENTATION, "withimplementation", false, "indicate that the model contains the code model");
		opt.setRequired(false);
		opt.setType(Boolean.class);
		options.addOption(opt);

		CommandLineParser parser = new DefaultParser();
		return parser.parse(options, args);

	}
}
