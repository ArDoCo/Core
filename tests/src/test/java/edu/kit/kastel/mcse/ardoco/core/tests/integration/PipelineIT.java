/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

class PipelineIT {

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String TEXT = "src/test/resources/benchmark/teastore/teastore.txt";
    private static final String MODEL = "src/test/resources/benchmark/teastore/original_model/teastore.repository";
    private static final String NAME = "test_teastore";

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("log4j.configurationFile", "src/main/resources/log4j2.xml");
    }

    @Test
    @DisplayName("Integration Test with provided text file")
    void pipelineWithTextIT() {
        String[] args = { "-n", NAME, "-ma", MODEL, "-t", TEXT, "-o", OUTPUT };
        Assertions.assertNotNull(args);
        Pipeline.main(args);
    }

    @Test
    @DisplayName("Integration Test without provided text file")
    void pipelineWithProvidedWrongTextOntologyIT() {
        String[] args = { "-n", NAME, "-ma", MODEL, "-p", "-o", OUTPUT };
        Assertions.assertNotNull(args);
        Pipeline.main(args);
    }

    @Test
    @DisplayName("Integration Test with wrong text")
    void pipelineWithNonexistentTextIT() {
        String[] args = { "-n", NAME, "-ma", MODEL, "-t", "NONEXISTENT", "-o", OUTPUT };
        Assertions.assertNotNull(args);
        Pipeline.main(args);
    }

    @Test
    @DisplayName("Integration Test with wrong model")
    void pipelineWithNonexistentModelIT() {
        String[] args = { "-n", NAME, "-ma", "NONEXISTENT", "-t", TEXT, "-o", OUTPUT };
        Assertions.assertNotNull(args);
        Pipeline.main(args);
    }

}
