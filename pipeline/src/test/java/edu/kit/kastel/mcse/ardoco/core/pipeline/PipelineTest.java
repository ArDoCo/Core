package edu.kit.kastel.mcse.ardoco.core.pipeline;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class PipelineTest {

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String TEXT = "src/test/resources/teastore.txt";
    private static final String MODEL = "src/test/resources/teastore.owl";
    private static final String NAME = "test_teastore";

    @BeforeEach
    void beforeEach() {
        // TODO
    }

    @AfterEach
    void afterEach() {
        // TODO
    }

    @Test
    @DisplayName("Integration Test")
    void integrationTest() {
        String[] args = { "-n", NAME, "-m", MODEL, "-t", TEXT, "-o", OUTPUT };
        Assertions.assertNotNull(args);
        Pipeline.main(args);
    }

}
