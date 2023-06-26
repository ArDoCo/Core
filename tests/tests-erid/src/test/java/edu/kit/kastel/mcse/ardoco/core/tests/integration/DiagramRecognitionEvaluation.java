package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoForERID;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.DiagramProject;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DiagramRecognitionEvaluation {
    private static final String OUTPUT_DIR = "src/test/resources/testout";

    private static List<DiagramProject> getHistoricalProjects() {
        return filterForHistoricalProjects(List.of(DiagramProject.values()));
    }

    private static List<DiagramProject> getNonHistoricalProjects() {
        return filterForNonHistoricalProjects(List.of(DiagramProject.values()));
    }

    private static <T extends Enum<T>> List<T> filterForHistoricalProjects(Collection<T> unfilteredProjects) {
        return filterForProjects(unfilteredProjects, p -> p.name().endsWith("HISTORICAL"));
    }

    private static <T extends Enum<T>> List<T> filterForNonHistoricalProjects(Collection<T> unfilteredProjects) {
        return filterForProjects(unfilteredProjects, p -> !p.name().endsWith("HISTORICAL"));
    }

    private static <T extends Enum<T>> List<T> filterForProjects(Collection<T> unfilteredProjects, Predicate<T> filter) {
        List<T> projects = new ArrayList<>();
        for (var project : unfilteredProjects) {
            if (filter.test(project)) {
                projects.add(project);
            }
        }
        return projects;
    }

    @DisplayName("Evaluate Diagram Recognition")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNonHistoricalProjects")
    @Order(1)
    void evaluateHistoricalDiagramRecognition(DiagramProject project) {
        run(project);
    }

    @DisplayName("Evaluate Diagram Recognition (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getHistoricalProjects")
    @Order(2)
    void evaluateNonHistoricalDiagramRecognition(DiagramProject project) {

    }

    private void run(DiagramProject project) {
        var runner = new ArDoCoForERID(project.name());
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(project.getAdditionalConfigurationsFile());
        runner.setUp(project.getDiagramsGoldStandardFile(), project.getTextFile(), project.getModelFile(), ArchitectureModelType.PCM, additionalConfigsMap,
                new File(OUTPUT_DIR));

        var result = runner.run();
        Assertions.assertNotNull(result);
        var diagramRecognition = result.dataRepository().getData(DiagramRecognitionState.ID, DiagramRecognitionState.class);
        Assertions.assertTrue(diagramRecognition.isPresent());
        Assertions.assertEquals(1, diagramRecognition.get().getDiagrams().size());
        var diagram = diagramRecognition.get().getDiagrams().get(0);
        //Assertions.assertEquals(6, diagram.getBoxes().stream().filter(it -> it.getClassification() != LABEL).count());
    }
}
