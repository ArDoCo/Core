package edu.kit.kastel.mcse.ardoco.tests.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.tests.TestRunner;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DiagramConnectionGeneratorTest {
    private static final Logger logger = LoggerFactory.getLogger(DiagramConnectionGeneratorTest.class);
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

    @DisplayName("Evaluate Diagram Connection Generator")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNonHistoricalProjects")
    @Order(1)
    void evaluateNonHistoricalDiagramRecognition(DiagramProject project) {
        run(project);
    }

    @DisplayName("Evaluate Diagram Connection Generator (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getHistoricalProjects")
    @Order(2)
    void evaluateHistoricalDiagramRecognition(DiagramProject project) {
        run(project);
    }

    private void run(DiagramProject project) {
        logger.info("Evaluate Diagram Connection for {}", project.name());
        var runner = new TestRunner(project.name());
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(project.getAdditionalConfigurationsFile());
        var params = new TestRunner.Parameters(project.getDiagramsGoldStandardFile(), project.getTextFile(), project.getModelFile(), ArchitectureModelType.PCM,
                additionalConfigsMap, new File(OUTPUT_DIR), true);

        runner.setUp(params);
        runner.runWithoutSaving();

        var dataRepository = runner.getArDoCo().getDataRepository();
        var diagramRecognition = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).get();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).get();
        var diagrams = diagramRecognition.getDiagrams();
        //TODO Get Metamodel properly
        var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(project.getMetamodel());
        var diagramLinks = diagramConnectionState.getDiagramLinks();
        var traceLinks = diagramConnectionState.getTraceLinks();
        var goldStandardTraceLinks = project.getDiagramTextTraceLinksFromGoldstandard();

        var totalSentences = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).get().getText().getSentences().size();
        var totalDiagramElements = diagrams.stream().flatMap(d -> d.getBoxes().stream()).toList().size();
        var total = totalSentences * totalDiagramElements;
        //TODO
        var TP = goldStandardTraceLinks.stream().filter(g -> traceLinks.contains(g)).toList().size();
        var FP = traceLinks.stream().filter(t -> !goldStandardTraceLinks.contains(t)).toList().size();
        var fnLinks = goldStandardTraceLinks.stream().filter(g -> !traceLinks.contains(g)).toList();
        var FN = fnLinks.size();
        var TN = total - TP - FP - FN;
        var P = TP / (double) (TP + FP);
        var R = TP / (double) (TP + FN);
        var acc = (TP + TN) / (double) (TP + TN + FP + FN);
        logger.info("TP:{}, FP:{}, TN:{}, FN:{}, P:{}, R:{}, Acc:{}", TP, FP, TN, FN, P, R, acc);
    }
}
