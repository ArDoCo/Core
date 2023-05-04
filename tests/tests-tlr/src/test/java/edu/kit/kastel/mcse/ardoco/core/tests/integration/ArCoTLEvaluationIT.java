/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;

public class ArCoTLEvaluationIT {
    private static final Logger logger = LoggerFactory.getLogger(ArCoTLEvaluationIT.class);

    private static final String OUTPUT = "src/test/resources/testout";
    private static final Path OUTPUT_PATH = Path.of(OUTPUT);
    private static final String ADDITIONAL_CONFIG = null;

    private static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @AfterAll
    public static void afterAll() {
        // TODO write results
        System.setProperty(LOGGING_ARDOCO_CORE, "error");
    }

    @AfterEach
    void afterEach() {
        if (ADDITIONAL_CONFIG != null) {
            var config = new File(ADDITIONAL_CONFIG);
            config.delete();
        }
    }

    @DisplayName("Evaluate TLR")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(value = Project.class, mode = EnumSource.Mode.MATCH_NONE, names = "^.*HISTORICAL$")
    @Order(1)
    void evaluateTraceLinkRecoveryIT(Project project) {
        runTraceLinkEvaluation(project);
    }

    @DisplayName("Evaluate TLR (Historical)")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(value = Project.class, mode = EnumSource.Mode.MATCH_ALL, names = "^.*HISTORICAL$")
    @Order(2)
    void evaluateHistoricalDataTraceLinkRecoveryIT(Project project) {
        runTraceLinkEvaluation(project);
    }

    private void runTraceLinkEvaluation(Project project) {
        System.out.println(project.name());
        //TODO
    }
}
