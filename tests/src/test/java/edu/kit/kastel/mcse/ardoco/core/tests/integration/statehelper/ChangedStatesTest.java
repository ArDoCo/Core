/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper.files.ConnectionStateFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper.files.RecommendationStateFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper.files.TextStateFile;

@Disabled("Disabled since it compares two testouts")
public class ChangedStatesTest {
    private static final boolean OVERWRITE_PREVIOUS = false;
    private static final Logger logger = LoggerFactory.getLogger(ChangedStatesTest.class);

    private static final String OUTPUT = "src/test/resources/testout";
    private static final Path OUTPUT_PATH = Path.of(OUTPUT);
    private static final Map<Project, ArDoCoResult> DATA_MAP = new HashMap<>();
    private static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @BeforeAll
    static void evaluateStates() throws IOException {
        for (Project project : Project.values()) {
            File inputModel = project.getModelFile();
            File inputText = project.getTextFile();
            String name = project.name().toLowerCase();
            ArDoCo arDoCo = ArDoCo.getInstance(name);

            var arDoCoResult = DATA_MAP.get(project);
            if (arDoCoResult == null) {
                arDoCoResult = arDoCo.runAndSave(name, inputText, inputModel, ArchitectureModelType.PCM, null, null, new File(OUTPUT));
                DATA_MAP.put(project, arDoCoResult);
            }
        }
    }

    private static boolean writeTextStateDiff(Project project, ArDoCoResult arDoCoResult) throws IOException {
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

        if (OVERWRITE_PREVIOUS) {
            try {
                Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                TextStateFile.write(currentPath, arDoCoResult);
                Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            if (!Files.exists(previousPath)) {
                throw new IllegalStateException("There is nothing to compare with - please set the overwrite option in ChangedStatesTest!");
            }
        }

        return TextStateFile.writeDiff(previousPath, currentPath, diffPath, arDoCoResult);
    }

    private static boolean writeRecommendationStateDiff(Project project, ArDoCoResult arDoCoResult) throws IOException {
        String name = project.name().toLowerCase();

        boolean result = true;

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
            if (OVERWRITE_PREVIOUS) {
                try {
                    Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    RecommendationStateFile.write(currentPath, arDoCoResult, metaModel);
                    Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                if (!Files.exists(previousPath)) {
                    throw new IllegalStateException("There is nothing to compare with - please set the overwrite option in ChangedStatesTest!");
                }
            }

            var tempResult = RecommendationStateFile.writeDiff(previousPath, currentPath, diffPath, arDoCoResult, metaModel);
            if (!tempResult)
                result = tempResult;
        }
        return result;
    }

    private static boolean writeConnectionStateDiff(Project project, ArDoCoResult arDoCoResult) throws IOException {
        String name = project.name().toLowerCase();

        boolean result = true;

        for (var modelId : arDoCoResult.getModelIds()) {

            var metaModel = arDoCoResult.getModelState(modelId).getMetamodel();
            var evalDir = Path.of(OUTPUT).resolve(name).resolve("connectionStates").resolve(metaModel.name());
            try {
                Files.createDirectories(evalDir);
            } catch (IOException e) {
                logger.warn("Could not create directories.", e);
            }

            var currentPath = evalDir.resolve("connectionState_" + metaModel.name() + "_" + name + ".txt");
            var previousPath = evalDir.resolve("connectionState_" + metaModel.name() + "_" + name + "_previous" + ".txt");
            var diffPath = evalDir.resolve("connectionState_" + metaModel.name() + "_" + name + "_diff" + ".txt");
            if (OVERWRITE_PREVIOUS) {
                try {
                    Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    ConnectionStateFile.write(currentPath, arDoCoResult, modelId);
                    Files.copy(currentPath, previousPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                if (!Files.exists(previousPath)) {
                    throw new IllegalStateException("There is nothing to compare with - please set the overwrite option in ChangedStatesTest!");
                }
            }
            var tempResult = ConnectionStateFile.writeDiff(previousPath, currentPath, diffPath, arDoCoResult, modelId);
            if (!tempResult)
                result = tempResult;
        }
        return result;
    }

    @DisplayName("Text State Diff")
    @ParameterizedTest(name = "Generating Diff of {0}")
    @EnumSource(value = Project.class)
    @Order(2)
    void textStateDiffTest(Project project) throws IOException {
        Assumptions.assumeTrue(DATA_MAP.containsKey(project));

        String name = project.name().toLowerCase() + "/";
        var path = OUTPUT_PATH.resolve(name);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }

        Assertions.assertTrue(writeTextStateDiff(project, DATA_MAP.get(project)));
    }

    @DisplayName("Recommendation State Diff")
    @ParameterizedTest(name = "Generating Diff of {0}")
    @EnumSource(value = Project.class)
    @Order(2)
    void recommendationStateDiffTest(Project project) throws IOException {
        Assumptions.assumeTrue(DATA_MAP.containsKey(project));

        String name = project.name().toLowerCase() + "/";
        var path = OUTPUT_PATH.resolve(name);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }

        Assertions.assertTrue(writeRecommendationStateDiff(project, DATA_MAP.get(project)));
    }

    @DisplayName("Connection State Diff")
    @ParameterizedTest(name = "Generating Diff of {0}")
    @EnumSource(value = Project.class)
    @Order(2)
    void connectionStateDiffTest(Project project) throws IOException {
        Assumptions.assumeTrue(DATA_MAP.containsKey(project));

        String name = project.name().toLowerCase() + "/";
        var path = OUTPUT_PATH.resolve(name);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }

        Assertions.assertTrue(writeConnectionStateDiff(project, DATA_MAP.get(project)));
    }

}
