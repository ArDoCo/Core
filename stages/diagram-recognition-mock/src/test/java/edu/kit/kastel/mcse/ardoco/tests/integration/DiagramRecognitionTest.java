package edu.kit.kastel.mcse.ardoco.tests.integration;

import java.io.IOException;
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

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramDataMock;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.erid.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DiagramRecognitionTest {
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
    void evaluateNonHistoricalDiagramRecognition(DiagramProject project) {
        run(project);
    }

    @DisplayName("Evaluate Diagram Recognition (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getHistoricalProjects")
    @Order(2)
    void evaluateHistoricalDiagramRecognition(DiagramProject project) {
        run(project);
    }

    private void run(DiagramProject project) {
        var dataRepository = new AnonymousRunner(project.name()) {
            @Override
            public void initializePipelineSteps() throws IOException {
                ArDoCo arDoCo = getArDoCo();
                var dataRepository = arDoCo.getDataRepository();

                var data = new InputDiagramDataMock(project);
                dataRepository.addData(InputDiagramDataMock.ID, data);

                arDoCo.addPipelineStep(new DiagramRecognitionMock(project.getAdditionalConfigurations(), dataRepository));
            }
        }.runWithoutSaving();
        var diagramRecognition = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).orElseThrow();
        var diagrams = diagramRecognition.getDiagrams();
        Assertions.assertTrue(diagrams.size() > 0);
    }
}
