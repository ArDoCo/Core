package edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper.files.RecommendationStateFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper.files.TextStateFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLProjectEvalResult;

public class ProjectEvalResult {
    private static final Logger logger = LoggerFactory.getLogger(ProjectEvalResult.class);

    private static final String OUTPUT = "src/test/resources/testout";
    private static final Path OUTPUT_PATH = Path.of(OUTPUT);
    private static final String ADDITIONAL_CONFIG = null;
    private static final List<TLProjectEvalResult> RESULTS = new ArrayList<>();
    private static final Map<Project, ArDoCoResult> DATA_MAP = new HashMap<>();
    private static final boolean detailedDebug = true;
    private static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";
    private String name;
    private File inputText;
    private File inputModel;
    private File inputCodeModel = null;
    private File additionalConfigs = null;
    private final File outputDir = new File(OUTPUT);

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @AfterEach
    void afterEach() {
        if (ADDITIONAL_CONFIG != null) {
            var config = new File(ADDITIONAL_CONFIG);
            config.delete();
        }
        if (additionalConfigs != null) {
            additionalConfigs = null;
        }
    }

    // NOTE: if you only want to test a specific project, you can simply set up the
    // EnumSource. For more details, see
    // https://www.baeldung.com/parameterized-tests-junit-5#3-enum
    // Example: add ", names = { "BIGBLUEBUTTON" }" to EnumSource
    // However, make sure to revert this before you commit and push!
    @DisplayName("Evaluate TLR (Text-based)")
    @ParameterizedTest(name = "Evaluating {0} (Text)")
    @EnumSource(value = Project.class)
    void evaluateStates(Project project) throws IOException {
        inputModel = project.getModelFile();
        inputText = project.getTextFile();
        name = project.name().toLowerCase();
        ArDoCo arDoCo = ArDoCo.getInstance(name);

        var arDoCoResult = DATA_MAP.get(project);
        if (arDoCoResult == null) {
            arDoCoResult = arDoCo.runAndSave(name, inputText, inputModel, ArchitectureModelType.PCM, inputCodeModel, additionalConfigs, outputDir);
            DATA_MAP.put(project, arDoCoResult);
        }

        writeDetailedOutput(project, arDoCoResult);
    }

    private static void writeTextStateDiff(Project project, ArDoCoResult arDoCoResult) throws IOException {
        String name = project.name().toLowerCase();
        var evalDir = Path.of(OUTPUT).resolve(name).resolve("textState");
        try {
            Files.createDirectories(evalDir);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }

        var currentPath = evalDir.resolve("textState_" + name + ".txt");
        var previousPath = evalDir.resolve("textState_" + name + "_previous" + ".txt");
        var diffPath = evalDir.resolve("textState_" + name + "_diff" + ".txt");
        try {
            Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            TextStateFile.write(currentPath, arDoCoResult);
            Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);
        }

        TextStateFile.writeDiff(previousPath, currentPath, diffPath, arDoCoResult);
    }

    private static void writeRecommendationStateDiff(Project project, ArDoCoResult arDoCoResult) throws IOException {
        String name = project.name().toLowerCase();

        for (var modelId : arDoCoResult.getModelIds()) {

            var metaModel = arDoCoResult.getModelState(modelId).getMetamodel();
            var evalDir = Path.of(OUTPUT).resolve(name).resolve("recommendationStates").resolve(metaModel.name());
            try {
                Files.createDirectories(evalDir);
            } catch (IOException e) {
                logger.warn("Could not create directories.", e);
            }

            var currentPath = evalDir.resolve("recommendationState_" + metaModel.name() + "_" + name + ".txt");
            var previousPath = evalDir.resolve("recommendationState_" + metaModel.name() + "_" + name + "_previous" + ".txt");
            var diffPath = evalDir.resolve("recommendationState_" + metaModel.name() + "_" + name + "_diff" + ".txt");
            try {
                Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                RecommendationStateFile.write(currentPath, arDoCoResult, metaModel);
                Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);
            }

            RecommendationStateFile.writeDiff(previousPath, currentPath, diffPath, arDoCoResult, metaModel);
        }
    }

    private static void writeDetailedOutput(Project project, ArDoCoResult arDoCoResult) throws IOException {
        String name = project.name().toLowerCase() + "/";
        var path = OUTPUT_PATH.resolve(name);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }

        writeTextStateDiff(project, arDoCoResult);
        writeRecommendationStateDiff(project, arDoCoResult);
    }

}
