/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import static edu.kit.kastel.mcse.ardoco.core.common.JsonHandling.createObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelLinkState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Extractions;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.InconsistencyType;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramConsistency;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramMatchingModelSelectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramModelLinkStateImpl;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.EvaluationTestBase;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.IntegerMetrics;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.MapMetrics;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.Metrics;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.MetricsStats;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedDiagram;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage1.ElementIdentification;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage1.Occurrence;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage2.ElementLinks;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage3.DiagramInconsistencies;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Disconnect;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Refactoring;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.RefactoringBundle;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Rename;

class DiagramConsistencyTest extends EvaluationTestBase {
    private static final Map<DiagramProject, double[]> expectedFinalJaccardResults = Map.of(DiagramProject.BIG_BLUE_BUTTON, new double[] { 0.69, 0.38, 0.89,
            0.72, 0.80 }, DiagramProject.TEAMMATES_ARCHITECTURE, new double[] { 0.33, 0.33, 1.00, 0.35, 0.52 }, DiagramProject.TEAMMATES_PACKAGES,
            new double[] { 0.52, 0.00, 0.63, 0.72, 0.68 }, DiagramProject.TEAMMATES_UI, new double[] { 0.48, 0.40, 0.67, 1.00, 0.80 }, DiagramProject.TEA_STORE,
            new double[] { 1.00, 0.17, 0.80, 0.94, 0.86 }, DiagramProject.MEDIA_STORE, new double[] { 0.82, 0.09, 0.68, 0.72, 0.70 });
    private static final Map<DiagramProject, double[]> expectedFinalLevenshteinResults = Map.of(DiagramProject.BIG_BLUE_BUTTON, new double[] { 0.92, 0.77, 0.85,
            0.81, 0.83 }, DiagramProject.TEAMMATES_ARCHITECTURE, new double[] { 0.27, 0.27, 1.00, 0.26, 0.41 }, DiagramProject.TEAMMATES_PACKAGES,
            new double[] { 0.40, 0.12, 0.84, 0.58, 0.69 }, DiagramProject.TEAMMATES_UI, new double[] { 0.36, 0.28, 1.00, 0.93, 0.96 }, DiagramProject.TEA_STORE,
            new double[] { 1.00, 0.00, 0.81, 1.00, 0.89 }, DiagramProject.MEDIA_STORE, new double[] { 0.91, 0.18, 0.82, 0.75, 0.78 });
    private final static Map<DiagramProject, double[]> expectedStage2TunedParameters = Map.of(DiagramProject.BIG_BLUE_BUTTON, new double[] { 1.00, 1.00, 1.00 },
            DiagramProject.TEAMMATES_ARCHITECTURE, new double[] { 1.00, 0.75, 0.86 }, DiagramProject.TEAMMATES_PACKAGES, new double[] { 0.88, 0.95, 0.91 },
            DiagramProject.TEAMMATES_UI, new double[] { 0.77, 1.00, 0.87 }, DiagramProject.TEA_STORE, new double[] { 0.67, 0.67, 0.67 },
            DiagramProject.MEDIA_STORE, new double[] { 0.82, 0.90, 0.86 });

    private final static Map<DiagramProject, double[]> expectedStage3 = Map.of(DiagramProject.BIG_BLUE_BUTTON, new double[] { 1.00, 1.00, 1.00 },
            DiagramProject.TEAMMATES_ARCHITECTURE, new double[] { 0.76, 0.76, 0.76 }, DiagramProject.TEAMMATES_PACKAGES, new double[] { 0.71, 0.92, 0.80 },
            DiagramProject.TEAMMATES_UI, new double[] { 0.73, 0.85, 0.78 }, DiagramProject.TEA_STORE, new double[] { 0.60, 0.47, 0.53 },
            DiagramProject.MEDIA_STORE, new double[] { 0.79, 0.73, 0.76 });

    private static Decision rateStage1Decision(DiagramProject project, DiagramMatchingModelSelectionState selection) {
        Decision decision;
        if (selection.getSelection().size() > 1) {
            decision = Decision.Both;
        } else if (selection.getSelection().size() == 1) {
            if (selection.getSelection().contains(project.getModelType())) {
                decision = Decision.Correct;
            } else {
                decision = Decision.Incorrect;
            }
        } else {
            decision = Decision.None;
        }
        return decision;
    }

    @DisplayName("Evaluate the model consistency pipeline (Stage 1) with initial parameters")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage1WithInitialParameters(DiagramProject project) throws IOException {
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramElementOccurrenceFinderInformant::similarityThresholdArchitecture", String.valueOf(0.5));
        config.put("DiagramElementOccurrenceFinderInformant::similarityThresholdCode", String.valueOf(0.75));
        config.put("DiagramElementOccurrenceFinderInformant::similarityFunction", String.valueOf(
                DiagramElementOccurrenceFinderInformant.TextSimilarityFunction.LEVENSHTEIN));
        config.put("OccurrenceToDecisionInformant::matchThreshold", String.valueOf(0.05));
        config.put("OccurrenceToDecisionInformant::matchDelta", String.valueOf(0.05));

        DecisionInfo result = this.runAndEvaluateStage1(project, config);
        assertNotNull(result);

        this.writeStage1Result(project, result);
    }

    @DisplayName("Evaluate the model consistency pipeline (Stage 1) with initial parameters")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage1WithInitialParametersWithJaccard(DiagramProject project) throws IOException {
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramElementOccurrenceFinderInformant::similarityThresholdArchitecture", String.valueOf(0.5));
        config.put("DiagramElementOccurrenceFinderInformant::similarityThresholdCode", String.valueOf(0.75));
        config.put("DiagramElementOccurrenceFinderInformant::similarityFunction", String.valueOf(
                DiagramElementOccurrenceFinderInformant.TextSimilarityFunction.ADAPTED_JACCARD));
        config.put("OccurrenceToDecisionInformant::matchThreshold", String.valueOf(0.05));
        config.put("OccurrenceToDecisionInformant::matchDelta", String.valueOf(0.05));

        DecisionInfo result = this.runAndEvaluateStage1(project, config);
        assertNotNull(result);

        this.writeStage1Result(project, result);
    }

    private void writeStage1Result(DiagramProject project, DecisionInfo result) throws IOException {
        Decision decision = result.decision();
        Metrics metrics = result.metrics();

        this.writer.write(String.format("Metrics for %s (decision: %s)%n", project.name(), decision));
        logger.info("Metrics for {} (decision: {})", project.name(), decision);
        this.writer.write(String.format("Scores: %.2f, %.2f, delta: %.2f%n", result.correctScore(), result.incorrectScore(), result.correctScore() - result
                .incorrectScore()));
        logger.info("Scores: {}, {}, delta: {}", result.correctScore(), result.incorrectScore(), result.correctScore() - result.incorrectScore());
        this.writer.write(String.format("Precision: %.2f%n", metrics.getPrecision()));
        logger.info("Precision: {}", metrics.getPrecision());
        this.writer.write(String.format("Recall: %.2f%n", metrics.getRecall()));
        logger.info("Recall: {}", metrics.getRecall());
        this.writer.write(String.format("F1: %.2f%n", metrics.getF1Score()));
        logger.info("F1: {}", metrics.getF1Score());
    }

    @DisplayName("Evaluate the model consistency pipeline 'similarityThresholdArchitecture' parameter (Stage 1)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage1ArchitectureThresholdParameterForLevenshtein() throws IOException {
        this.evaluateStage1Parameter("similarityThresholdArchitecture", DiagramElementOccurrenceFinderInformant.TextSimilarityFunction.LEVENSHTEIN);
    }

    @DisplayName("Evaluate the model consistency pipeline 'similarityThresholdArchitecture' parameter (Stage 1)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage1ArchitectureThresholdParameterForJaccard() throws IOException {
        this.evaluateStage1Parameter("similarityThresholdArchitecture", DiagramElementOccurrenceFinderInformant.TextSimilarityFunction.ADAPTED_JACCARD);
    }

    @DisplayName("Evaluate the model consistency pipeline 'similarityThresholdCode' parameter (Stage 1)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage1CodeThresholdParameterForLevenshtein() throws IOException {
        this.evaluateStage1Parameter("similarityThresholdCode", DiagramElementOccurrenceFinderInformant.TextSimilarityFunction.LEVENSHTEIN);
    }

    @DisplayName("Evaluate the model consistency pipeline 'similarityThresholdCode' parameter (Stage 1)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage1CodeThresholdParameterForJaccard() throws IOException {
        this.evaluateStage1Parameter("similarityThresholdCode", DiagramElementOccurrenceFinderInformant.TextSimilarityFunction.ADAPTED_JACCARD);
    }

    private void evaluateStage1Parameter(String parameter, DiagramElementOccurrenceFinderInformant.TextSimilarityFunction textSimilarityFunction)
            throws IOException {
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramElementOccurrenceFinderInformant::similarityThresholdArchitecture", String.valueOf(0.5));
        config.put("DiagramElementOccurrenceFinderInformant::similarityThresholdCode", String.valueOf(0.75));
        config.put("DiagramElementOccurrenceFinderInformant::similarityFunction", String.valueOf(textSimilarityFunction));
        config.put("OccurrenceToDecisionInformant::matchThreshold", String.valueOf(0.05));
        config.put("OccurrenceToDecisionInformant::matchDelta", String.valueOf(0.05));

        for (int i = 0; i <= 100; i++) {
            double value = 0.01 * i;
            config.put("DiagramElementOccurrenceFinderInformant::" + parameter, String.valueOf(value));

            double averageF1Score = 0.0;
            double averageCorrectScore = 0.0;
            double averageIncorrectScore = 0.0;

            for (DiagramProject project : DiagramProject.values()) {
                DecisionInfo result = this.runAndEvaluateStage1(project, config);
                assertNotNull(result);

                averageF1Score += result.metrics().getF1Score();
                averageCorrectScore += result.correctScore();
                averageIncorrectScore += result.incorrectScore();
            }

            averageF1Score /= DiagramProject.values().length;
            averageCorrectScore /= DiagramProject.values().length;
            averageIncorrectScore /= DiagramProject.values().length;

            this.writer.write(String.format(Locale.US, "%.3f,%.3f,%.3f,%.3f%n", value, averageF1Score, averageCorrectScore, averageIncorrectScore));
        }
    }

    @DisplayName("Evaluate the model consistency pipeline (Stage 1) with final parameters")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void evaluateStage1VersionFinalJaccard(DiagramProject project) throws IOException {
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramElementOccurrenceFinderInformant::similarityThresholdArchitecture", String.valueOf(0.6));
        config.put("DiagramElementOccurrenceFinderInformant::similarityThresholdCode", String.valueOf(0.8));
        config.put("DiagramElementOccurrenceFinderInformant::similarityFunction", String.valueOf(
                DiagramElementOccurrenceFinderInformant.TextSimilarityFunction.ADAPTED_JACCARD));
        config.put("OccurrenceToDecisionInformant::matchThreshold", String.valueOf(0.05));
        config.put("OccurrenceToDecisionInformant::matchDelta", String.valueOf(0.05));

        DecisionInfo result = this.runAndEvaluateStage1(project, config);
        assertNotNull(result);

        this.writeStage1Result(project, result);
        this.assertStage1Result(expectedFinalJaccardResults, project, result);

        assertTrue(result.decision() == Decision.Correct || result.decision() == Decision.Both);
    }

    @DisplayName("Evaluate the model consistency pipeline (Stage 1) final parameters")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void evaluateStage1VersionFinalLevenshtein(DiagramProject project) throws IOException {
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramElementOccurrenceFinderInformant::similarityThresholdArchitecture", String.valueOf(0.5));
        config.put("DiagramElementOccurrenceFinderInformant::similarityThresholdCode", String.valueOf(0.8));
        config.put("DiagramElementOccurrenceFinderInformant::similarityFunction", String.valueOf(
                DiagramElementOccurrenceFinderInformant.TextSimilarityFunction.LEVENSHTEIN));
        config.put("OccurrenceToDecisionInformant::matchThreshold", String.valueOf(0.05));
        config.put("OccurrenceToDecisionInformant::matchDelta", String.valueOf(0.05));

        DecisionInfo result = this.runAndEvaluateStage1(project, config);
        assertNotNull(result);

        this.writeStage1Result(project, result);
        this.assertStage1Result(expectedFinalLevenshteinResults, project, result);
    }

    private void assertStage1Result(Map<DiagramProject, double[]> allExpected, DiagramProject project, DecisionInfo result) {
        double[] expected = allExpected.get(project);
        assertNotNull(expected);
        assertEquals(expected[0], Math.max(result.correctScore(), result.incorrectScore()), 0.01);
        assertEquals(expected[1], Math.abs(result.correctScore() - result.incorrectScore()), 0.01);
        assertEquals(expected[2], result.metrics().getPrecision(), 0.01);
        assertEquals(expected[3], result.metrics().getRecall(), 0.01);
        assertEquals(expected[4], result.metrics().getF1Score(), 0.01);
    }

    @DisplayName("Evaluate the model consistency pipeline (Stage 2) with initial parameters")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage2Initial(DiagramProject project) throws IOException {
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramModelLinkInformant::textSimilarityThreshold", String.valueOf(0.5));
        config.put("DiagramModelLinkInformant::similarityThreshold", String.valueOf(Double.NEGATIVE_INFINITY));

        Metrics metrics = this.runAndEvaluateStage2(project, config);
        this.writeStage2Result(project, metrics);

        assertTrue(metrics.getF1Score() > 0.0);
    }

    private void writeStage2Result(DiagramProject project, Metrics metrics) throws IOException {
        this.writer.write(String.format("Metrics for %s%n", project.name()));
        logger.info("Metrics for {}", project.name());
        this.writer.write(String.format("Precision: %.2f%n", metrics.getPrecision()));
        logger.info("Precision: {}", metrics.getPrecision());
        this.writer.write(String.format("Recall: %.2f%n", metrics.getRecall()));
        logger.info("Recall: {}", metrics.getRecall());
        this.writer.write(String.format("F1: %.2f%n", metrics.getF1Score()));
        logger.info("F1: {}", metrics.getF1Score());
    }

    @DisplayName("Evaluate the model consistency pipeline (Stage 2) using a similarity threshold")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage2WithSimilarityThreshold(DiagramProject project) throws IOException {
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramModelLinkInformant::textSimilarityThreshold", String.valueOf(0.5));
        config.put("DiagramModelLinkInformant::similarityThreshold", String.valueOf(0.1));

        Metrics metrics = this.runAndEvaluateStage2(project, config);
        this.writeStage2Result(project, metrics);

        assertTrue(metrics.getF1Score() > 0.0);
    }

    @DisplayName("Evaluate the model consistency pipeline (Stage 2) using new parameters")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void evaluateStage2TunedParameters(DiagramProject project) throws IOException {
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");

        Metrics metrics = this.runAndEvaluateStage2(project, config);
        this.writeStage2Result(project, metrics);

        this.assertStage2And3Result(project, expectedStage2TunedParameters, metrics);
    }

    private void assertStage2And3Result(DiagramProject project, Map<DiagramProject, double[]> allExpected, Metrics metrics) {
        double[] expected = allExpected.get(project);
        assertNotNull(expected);
        assertEquals(expected[0], metrics.getPrecision(), 0.01);
        assertEquals(expected[1], metrics.getRecall(), 0.01);
        assertEquals(expected[2], metrics.getF1Score(), 0.01);
    }

    @DisplayName("Evaluate the model consistency pipeline (Stage 2) using no iterations")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage2ZeroIterations(DiagramProject project) throws IOException {
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramModelLinkInformant::maxIterations", String.valueOf(0));

        Metrics metrics = this.runAndEvaluateStage2(project, config);
        this.writeStage2Result(project, metrics);

        assertTrue(metrics.getF1Score() > 0.0);
    }

    @DisplayName("Evaluate the best epsilon for the model consistency pipeline (Stage 2)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage2Epsilon() throws IOException {
        List<Double> epsilons = IntStream.range(1, 50).mapToDouble(i -> 0.05 * i).boxed().toList();
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramModelLinkInformant::epsilon", String.valueOf(1.0));
        config.put("DiagramModelLinkInformant::textSimilarityThreshold", String.valueOf(0.5));
        config.put("DiagramModelLinkInformant::similarityThreshold", String.valueOf(0.1));
        this.evaluateImpactOfParameterOnStage2("epsilon", epsilons, config);
    }

    @DisplayName("Evaluate the best levenshtein threshold for the model consistency pipeline (Stage 2)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage2Levenshtein() throws IOException {
        List<Double> thresholds = IntStream.range(0, 50).mapToDouble(i -> 0.02 * i).boxed().toList();
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramModelLinkInformant::epsilon", String.valueOf(1.0));
        config.put("DiagramModelLinkInformant::textSimilarityThreshold", String.valueOf(0.5));
        config.put("DiagramModelLinkInformant::similarityThreshold", String.valueOf(0.1));
        this.evaluateImpactOfParameterOnStage2("textSimilarityThreshold", thresholds, config);
    }

    @DisplayName("Evaluate the best similarity threshold for the model consistency pipeline (Stage 2)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage2SimilarityThreshold() throws IOException {
        List<Double> thresholds = IntStream.range(0, 50).mapToDouble(i -> 0.02 * i).boxed().toList();
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        config.put("DiagramModelLinkInformant::epsilon", String.valueOf(1.0));
        config.put("DiagramModelLinkInformant::textSimilarityThreshold", String.valueOf(0.5));
        config.put("DiagramModelLinkInformant::similarityThreshold", String.valueOf(0.1));
        this.evaluateImpactOfParameterOnStage2("similarityThreshold", thresholds, config);
    }

    private void evaluateImpactOfParameterOnStage2(String parameter, List<Double> values, SortedMap<String, String> config) throws IOException {
        this.evaluateImpactOnStage(values, 1, (project, value) -> {
            config.put("DiagramModelLinkInformant::" + parameter, String.valueOf(value));
            try {
                return this.runAndEvaluateStage2(project, config);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void evaluateImpactOnStage(List<Double> values, int repetition, BiFunction<DiagramProject, Double, Metrics> function) throws IOException {
        List<MetricsStats> extrema = new ArrayList<>();
        int totalEntryCount = 0;

        for (double value : values) {
            extrema.add(new MetricsStats());
            for (DiagramProject project : DiagramProject.values()) {
                for (int i = 0; i < repetition; i++) {
                    Metrics metrics = function.apply(project, value);
                    if (metrics == null) {
                        continue;
                    }
                    extrema.get(extrema.size() - 1).add(metrics, metrics.getTruePositiveCount());
                    totalEntryCount++;
                }
            }
        }

        assertNotEquals(0, totalEntryCount);

        for (int index = 0; index < values.size(); index++) {
            this.writer.write(String.format(Locale.US, "%.3f,%.3f,%.3f,%.3f,%.3f%n", values.get(index), extrema.get(index).getMinF1Score(), extrema.get(index)
                    .getMaxF1Score(), extrema.get(index).getAverageF1Score(), extrema.get(index).getWeightedAverageF1Score()));
        }
    }

    private DiagramConsistency setupAndRun(DiagramProject project, Refactoring<Box, Box> refactoring, double ratio, SortedMap<String, String> config,
            Consumer<DiagramConsistency> setup) throws IOException {
        String name = project.name().toLowerCase(Locale.ROOT);
        File inputArchitectureModel = project.getSourceProject().getModelFile(ArchitectureModelType.UML);
        File inputCodeModel = new File(Objects.requireNonNull(project.getSourceProject().getCodeModelDirectory())).getAbsoluteFile();
        File inputDiagram = this.getDiagramFile(project, refactoring, ratio);
        File outputDir = new File(PIPELINE_OUTPUT);

        if (inputDiagram == null) {
            return null;
        }

        DiagramConsistency runner = new DiagramConsistency(name);
        runner.setUp(inputArchitectureModel, inputCodeModel, inputDiagram, outputDir, config);
        setup.accept(runner);

        runner.run();

        return runner;
    }

    private DecisionInfo runAndEvaluateStage1(DiagramProject project, SortedMap<String, String> config) throws IOException {
        DiagramConsistency runner = this.setupAndRun(project, null, 0.0, config, r -> {
        });

        if (runner == null) {
            return null;
        }

        DiagramMatchingModelSelectionState selection = runner.getDataRepository()
                .getData(DiagramMatchingModelSelectionState.ID, DiagramMatchingModelSelectionStateImpl.class)
                .orElse(null);

        if (selection == null) {
            return null;
        }

        Decision decision = rateStage1Decision(project, selection);

        double correctScore = 0.0;
        double incorrectScore = 0.0;

        for (var score : selection.getSelectionExplanation().entrySet()) {
            if (score.getKey().equals(project.getModelType())) {
                correctScore = score.getValue();
            } else {
                incorrectScore = score.getValue();
            }
        }

        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

        ElementIdentification expectedIdentifications = createObjectMapper().readValue(project.getIdentificationStage(), ElementIdentification.class);

        for (var element : expectedIdentifications.elements()) {
            var actualOccurrences = selection.getOccurrences(String.valueOf(element.boxId()));
            Set<Occurrence> missingOccurrences = new LinkedHashSet<>(List.of(element.occurrences()));

            for (var actualOccurrence : actualOccurrences) {
                List<Occurrence> matches = missingOccurrences.stream()
                        .filter(occurrence -> occurrence.modelElementId().equals(actualOccurrence.modelID()) && occurrence.role()
                                .equals(actualOccurrence.role()))
                        .toList();

                if (matches.isEmpty()) {
                    falsePositives++;
                } else {
                    truePositives++;
                    matches.forEach(missingOccurrences::remove);
                }
            }

            falseNegatives += missingOccurrences.size();
        }

        return new DecisionInfo(decision, correctScore, incorrectScore, new IntegerMetrics(truePositives, falsePositives, falseNegatives));
    }

    private Metrics runAndEvaluateStage2(DiagramProject project, SortedMap<String, String> config) throws IOException {
        return this.runAndEvaluateStage2(project, null, 0.0, config);
    }

    private static Diagram getDiagram(DataRepository data) {
        return data.getData(DiagramState.ID, DiagramState.class).orElseThrow().getDiagram();
    }

    private File getDiagramFile(DiagramProject project, Refactoring<Box, Box> refactoring, double ratio) throws IOException {
        if (refactoring == null) {
            return project.getDiagramFile();
        }

        AnnotatedDiagram<Box> diagram = AnnotatedDiagram.createFrom(DiagramProviderInformant.load(project.getDiagramFile()));
        int size = diagram.diagram().getBoxes().size();
        int count = (int) (size * ratio);

        diagram = applyRefactoring(diagram, new RefactoringBundle<>(Map.of(refactoring, count)));
        if (diagram == null) {
            return null;
        }

        File refactored = File.createTempFile("temp", ".json");
        createObjectMapper().writeValue(refactored, diagram.diagram());

        return refactored;
    }

    @DisplayName("Evaluate the impact of renaming on the model consistency pipeline (Stage 2)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage2StabilityToRename() throws IOException {
        List<Double> ratios = IntStream.range(0, 50).mapToDouble(i -> 0.02 * i).boxed().toList();
        this.evaluateImpactOnStage(ratios, 10, (project, ratio) -> {
            SortedMap<String, String> config = new TreeMap<>();
            config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
            try {
                return this.runAndEvaluateStage2(project, new Rename<>(), ratio, config);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @DisplayName("Evaluate the impact of disconnecting on the model consistency pipeline (Stage 2)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage2StabilityToDisconnect() throws IOException {
        List<Double> ratios = IntStream.range(0, 50).mapToDouble(i -> 0.02 * i).boxed().toList();
        this.evaluateImpactOnStage(ratios, 10, (project, ratio) -> {
            SortedMap<String, String> config = new TreeMap<>();
            config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
            try {
                return this.runAndEvaluateStage2(project, new Disconnect<>(), ratio, config);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static Map<String, Entity> getModel(DiagramProject project, DataRepository data) {
        Model model = data.getData(ModelStates.ID, ModelStates.class).orElseThrow().getModel(project.getModelType().getModelId());

        return Extractions.extractEntitiesFromModel(model);
    }

    private Metrics runAndEvaluateStage2(DiagramProject project, Refactoring<Box, Box> refactoring, double ratio, SortedMap<String, String> config)
            throws IOException {
        DiagramConsistency runner = this.setupAndRun(project, refactoring, ratio, config, r -> {
            DiagramMatchingModelSelectionStateImpl modelSelection = new DiagramMatchingModelSelectionStateImpl();
            modelSelection.setSelection(Set.of(project.getModelType()));
            r.getDataRepository().addData(DiagramMatchingModelSelectionState.ID, modelSelection);
        });

        if (runner == null) {
            return null;
        }

        MutableBiMap<String, String> expectedLinks = createObjectMapper().readValue(project.getLinkingStage(), ElementLinks.class).toBiMap();
        MutableBiMap<String, String> foundLinks = runner.getDataRepository()
                .getData(DiagramModelLinkState.ID, DiagramModelLinkState.class)
                .orElseThrow()
                .getLinks(project.getModelType());

        return MapMetrics.from(expectedLinks, foundLinks);
    }

    @DisplayName("Evaluate the model consistency pipeline (Stage 3), skipping previous stages")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void evaluateStage3Solo(DiagramProject project) throws IOException {
        Metrics result = this.runAndEvaluateStage3(project, null, 0.0, new TreeMap<>(), true);
        assertNotNull(result);
        assertEquals(1.0, result.getF1Score());
    }

    @DisplayName("Evaluate the model consistency pipeline, using results of stage 3 as final results")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void evaluateAllStages(DiagramProject project) throws IOException {
        Metrics result = this.runAndEvaluateStage3(project, null, 0.0, new TreeMap<>(), false);
        assertNotNull(result);

        this.writeStage3Result(project, result);
        this.assertStage2And3Result(project, expectedStage3, result);
    }

    private Metrics runAndEvaluateStage3(DiagramProject project, Refactoring<Box, Box> refactoring, double ratio, SortedMap<String, String> config,
            boolean skipPreviousStages) throws IOException {
        MutableBiMap<String, String> expectedLinks = createObjectMapper().readValue(project.getLinkingStage(), ElementLinks.class).toBiMap();

        if (skipPreviousStages) {
            config.put("WeightedSimilarityInformant::skip", String.valueOf(true));
            config.put("DiagramElementOccurrenceFinderInformant::skip", String.valueOf(true));
            config.put("OccurrenceToDecisionInformant::skip", String.valueOf(true));
            config.put("DiagramModelLinkInformant::skip", String.valueOf(true));
        }

        DiagramConsistency runner = this.setupAndRun(project, refactoring, ratio, config, r -> {
            if (skipPreviousStages) {
                DiagramMatchingModelSelectionStateImpl modelSelection = new DiagramMatchingModelSelectionStateImpl();
                modelSelection.setSelection(Set.of(project.getModelType()));
                r.getDataRepository().addData(DiagramMatchingModelSelectionState.ID, modelSelection);

                DiagramModelLinkStateImpl matching = new DiagramModelLinkStateImpl();
                matching.setLinks(project.getModelType(), expectedLinks);
                r.getDataRepository().addData(DiagramModelLinkState.ID, matching);
            }
        });

        if (runner == null) {
            return null;
        }

        Diagram diagram = getDiagram(runner.getDataRepository());
        Map<String, Entity> model = getModel(project, runner.getDataRepository());

        DiagramModelInconsistencyState inconsistencyState = runner.getDataRepository()
                .getData(DiagramModelInconsistencyState.ID, DiagramModelInconsistencyState.class)
                .orElseThrow();

        List<Inconsistency<Box, Entity>> expected = createObjectMapper().readValue(project.getValidationStage(), DiagramInconsistencies.class)
                .toInconsistencies(diagram, model);

        Map<String, Box> boxes = DiagramUtility.getBoxes(diagram);

        List<Inconsistency<Box, Entity>> found = inconsistencyState.getInconsistencies(project.getModelType())
                .stream()
                .map(inconsistency -> inconsistency.map(boxes::get, model::get))
                .toList();

        List<Inconsistency<Box, Entity>> extendedFound = inconsistencyState.getExtendedInconsistencies(project.getModelType())
                .stream()
                .map(inconsistency -> inconsistency.map(boxes::get, model::get))
                .toList();

        for (var inconsistency : extendedFound) {
            this.writer.write(String.format("%s%n", inconsistency));
        }

        return MapMetrics.from(getMapBasedInconsistencySet(expected), getMapBasedInconsistencySet(found));
    }

    private void writeStage3Result(DiagramProject project, Metrics result) throws IOException {
        this.writer.write(String.format("Metrics for %s%n", project.name()));
        logger.info("Metrics for {}: {}", project.name(), result);
        this.writer.write(String.format("Precision: %.2f%n", result.getPrecision()));
        logger.info("Precision: {}", result.getPrecision());
        this.writer.write(String.format("Recall: %.2f%n", result.getRecall()));
        logger.info("Recall: {}", result.getRecall());
        this.writer.write(String.format("F1: %.2f%n", result.getF1Score()));
        logger.info("F1: {}", result.getF1Score());
    }

    @DisplayName("Evaluate the best epsilon (Stage 2) for the model consistency pipeline (Stage 3)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage3Epsilon() throws IOException {
        List<Double> epsilons = IntStream.range(1, 50).mapToDouble(i -> 0.05 * i).boxed().toList();
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        this.evaluateImpactOfStage2ParameterOnStage3("epsilon", epsilons, config);
    }

    @DisplayName("Evaluate the best levenshtein threshold (Stage 2) for the model consistency pipeline (Stage 3)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage3Levenshtein() throws IOException {
        List<Double> thresholds = IntStream.range(0, 50).mapToDouble(i -> 0.02 * i).boxed().toList();
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        this.evaluateImpactOfStage2ParameterOnStage3("textSimilarityThreshold", thresholds, config);
    }

    @DisplayName("Evaluate the best similarity threshold (Stage 2) for the model consistency pipeline (Stage 3)")
    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void evaluateStage3SimilarityThreshold() throws IOException {
        List<Double> thresholds = IntStream.range(0, 50).mapToDouble(i -> 0.02 * i).boxed().toList();
        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramRecognition::enabledAgents", "DiagramRecognitionAgent");
        this.evaluateImpactOfStage2ParameterOnStage3("similarityThreshold", thresholds, config);
    }

    private void evaluateImpactOfStage2ParameterOnStage3(String parameter, List<Double> values, SortedMap<String, String> config) throws IOException {
        this.evaluateImpactOnStage(values, 1, (project, value) -> {
            config.put("DiagramModelLinkInformant::" + parameter, String.valueOf(value));
            try {
                return this.runAndEvaluateStage3(project, null, 0.0, config, false);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    enum Decision {
        None, Correct, Incorrect, Both
    }

    record DecisionInfo(Decision decision, double correctScore, double incorrectScore, Metrics metrics) {
    }

    @Test
    @Disabled("No assertions, serves as evaluation runner only")
    void countInconsistencies() throws IOException {
        for (DiagramProject project : DiagramProject.values()) {
            DiagramInconsistencies inconsistencies = createObjectMapper().readValue(project.getValidationStage(), DiagramInconsistencies.class);

            Map<InconsistencyType, Integer> counts = new LinkedHashMap<>();
            for (var inconsistency : inconsistencies.inconsistencies()) {
                counts.merge(inconsistency.type(), 1, Integer::sum);
            }

            StringBuilder builder = new StringBuilder(String.format("%s - ", project.name()));
            for (var type : InconsistencyType.values()) {
                builder.append(String.format("%d, ", counts.getOrDefault(type, 0)));
            }
            builder.append(System.lineSeparator());
            this.writer.write(builder.toString());
        }
    }
}
