/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedDiagram;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Connect;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Create;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Delete;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Disconnect;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Move;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.RefactoringBundle;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Rename;

class SyntheticDiagramsTest extends EvaluationTestBase {
    @DisplayName("Apply refactorings to synthetic architecture diagram")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void refactorArchitectureModel(DiagramProject project) {
        RefactoringBundle<Box, ArchitectureItem> bundle = new RefactoringBundle<>(Map.of(new Connect<>(), 1, new Create<>(), 1, new Delete<>(), 1,
                new Disconnect<>(), 1, new Move<>(), 1, new Rename<>(), 1));

        AnnotatedDiagram<ArchitectureItem> diagram = applyRefactoring(getAnnotatedArchitectureDiagram(project), bundle);
        assertNotNull(diagram);
        assertFalse(diagram.inconsistencies().isEmpty());
    }

    @DisplayName("Apply refactorings to synthetic code diagram")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void refactorCodeModel(DiagramProject project) {
        RefactoringBundle<Box, CodeItem> bundle = new RefactoringBundle<>(Map.of(new Connect<>(), 2, new Create<>(), 2, new Delete<>(), 2, new Disconnect<>(),
                2, new Move<>(), 2, new Rename<>(), 2));

        AnnotatedDiagram<CodeItem> diagram = getAnnotatedCodeDiagram(project);
        assertTrue(diagram.diagram().getBoxes().size() > 6 * 2);

        diagram = applyRefactoring(diagram, bundle);

        assertNotNull(diagram);
        assertFalse(diagram.inconsistencies().isEmpty());
    }

    @DisplayName("Transform architecture diagram to graph and back to diagram")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void transformArchitectureDiagramIdentity(DiagramProject project) {
        AnnotatedDiagram<ArchitectureItem> diagram = getAnnotatedArchitectureDiagram(project);
        Set<String> texts = diagram.diagram().getBoxes().stream().map(DiagramUtility::getBoxText).collect(Collectors.toSet());

        AnnotatedGraph<Box, ArchitectureItem> graph = AnnotatedGraph.createFrom(diagram);
        diagram = AnnotatedDiagram.createFrom(diagram.diagram().getLocation().toString(), graph);
        assertEquals(texts, diagram.diagram().getBoxes().stream().map(DiagramUtility::getBoxText).collect(Collectors.toSet()));
    }

    @DisplayName("Transform code diagram to graph and back to diagram")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void transformCodeDiagramIdentity(DiagramProject project) {
        AnnotatedDiagram<CodeItem> diagram = getAnnotatedCodeDiagram(project);
        Set<String> texts = diagram.diagram().getBoxes().stream().map(DiagramUtility::getBoxText).collect(Collectors.toSet());

        AnnotatedGraph<Box, CodeItem> graph = AnnotatedGraph.createFrom(diagram);
        diagram = AnnotatedDiagram.createFrom(diagram.diagram().getLocation().toString(), graph);
        assertEquals(texts, diagram.diagram().getBoxes().stream().map(DiagramUtility::getBoxText).collect(Collectors.toSet()));
    }
}
