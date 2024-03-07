/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tests.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.id.tests.eval.DiagramProject;

class DiagramProjectTest {

    @DisplayName("Test Diagram Project")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.id.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(1)
    void getDiagramsFromGoldstandard(DiagramProject diagramProject) throws IOException {
        assertEquals(-1L, Files.mismatch(diagramProject.getDiagramsGoldStandardFile().toPath(), diagramProject.getDiagramsGoldStandardFile().toPath()));
    }
}
