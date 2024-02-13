/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Edge;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Label;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.TextSimilarity;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Transformations;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.FixpointFormula;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.OrderedMatchingFilter;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.PropagationCoefficientFormula;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.SimilarityFloodingAlgorithm;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.SimilarityMapping;
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

class SyntheticDiagramMatchingEvaluationTest extends EvaluationTestBase {
    private static final double DEFAULT_EPSILON = 0.075;

    @DisplayName("Compare architecture matching quality depending on inconsistent side")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void evaluateSymmetryOfArchitectureMatching(DiagramProject project) throws IOException {
        AnnotatedGraph<ArchitectureItem, ArchitectureItem> model = AnnotatedGraph.createFrom(getArchitectureModel(project));

        this.evaluateSymmetryOfMatching(getAnnotatedArchitectureDiagram(project), model);
    }

    @DisplayName("Compare code matching quality depending on inconsistent side")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    @Disabled("Requires a lot of memory")
    void evaluateSymmetryOfCodeMatching(DiagramProject project) throws IOException {
        AnnotatedGraph<CodeItem, CodeItem> model = AnnotatedGraph.createFrom(getCodeModel(project));

        this.evaluateSymmetryOfMatching(getAnnotatedCodeDiagram(project), model);
    }

    private <M> void evaluateSymmetryOfMatching(AnnotatedDiagram<M> diagram, AnnotatedGraph<M, M> model) throws IOException {
        final int modelSize = diagram.diagram().getBoxes().size();
        double ratio = 0.25;
        int iterationsPerSide = 2;

        this.writer.write(String.format("##### Diagram: %s, Model Size: %d%n", diagram.diagram().getLocation().toString(), modelSize));

        int count = (int) (modelSize * ratio);
        assertNotEquals(0, count);
        count = 1;

        MetricsStats diagramStats = new MetricsStats();

        RefactoringBundle<Box, M> diagramRefactoring = new RefactoringBundle<>(Map.of(new Connect<>(), count, new Create<>(), count, new Delete<>(), count,
                new Disconnect<>(), count, new Move<>(), count, new Rename<>(), count));

        this.doIterationsOnRefactoredDiagram(diagram, model, iterationsPerSide, diagramRefactoring, diagramStats);

        this.writer.write(String.format("[DIAGRAM] Refactorings: %d (%f%%), Precision: %f, Recall: %f, F1: %f%n", count, ratio * 100, diagramStats
                .getAveragePrecision(), diagramStats.getAverageRecall(), diagramStats.getAverageF1Score()));

        MetricsStats modelStats = new MetricsStats();

        RefactoringBundle<M, M> modelRefactoring = new RefactoringBundle<>(Map.of(new Connect<>(), count, new Create<>(), count, new Delete<>(), count,
                new Disconnect<>(), count, new Move<>(), count, new Rename<>(), count));

        var diagramGraph = Transformations.toGraph(diagram.diagram());
        Map<Box, Vertex<Box>> boxToVertex = diagramGraph.vertexSet().stream().collect(Collectors.toMap(Vertex::getRepresented, Function.identity()));

        this.doIterationsOnRefactoredModel(diagram, model, iterationsPerSide, modelRefactoring, boxToVertex, diagramGraph, modelStats);

        this.writer.write(String.format("[ MODEL ] Refactorings: %d (%f%%), Precision: %f, Recall: %f, F1: %f%n", count, ratio * 100, modelStats
                .getAveragePrecision(), modelStats.getAverageRecall(), modelStats.getAverageF1Score()));

        this.writer.write(String.format("DIFFERENCE: %f%n", diagramStats.getAverageF1Score() - modelStats.getAverageF1Score()));
    }

    private <M> void doIterationsOnRefactoredDiagram(AnnotatedDiagram<M> diagram, AnnotatedGraph<M, M> model, int iterationsPerSide,
            RefactoringBundle<Box, M> diagramRefactoring, MetricsStats diagramStats) {
        for (int iteration = 0; iteration < iterationsPerSide; iteration++) {
            AnnotatedDiagram<M> refactoredDiagram = applyRefactoring(diagram, diagramRefactoring);

            if (refactoredDiagram == null) {
                continue;
            }

            MutableBiMap<Vertex<Box>, Vertex<M>> links = this.match(Transformations.toGraph(refactoredDiagram.diagram()), model.graph());
            Metrics metrics = MapMetrics.from(refactoredDiagram.links(), this.simplifyMatches(links));

            diagramStats.add(metrics, 1.0);
        }
    }

    private <M> void doIterationsOnRefactoredModel(AnnotatedDiagram<M> diagram, AnnotatedGraph<M, M> model, int iterationsPerSide,
            RefactoringBundle<M, M> modelRefactoring, Map<Box, Vertex<Box>> boxToVertex, DirectedMultigraph<Vertex<Box>, Edge> diagramGraph,
            MetricsStats modelStats) {
        for (int iteration = 0; iteration < iterationsPerSide; iteration++) {
            AnnotatedGraph<M, M> refactoredModel = model.copy();
            boolean success = modelRefactoring.applyTo(refactoredModel);

            if (!success) {
                continue;
            }

            MutableBiMap<Vertex<Box>, Vertex<M>> expectedLinks = new HashBiMap<>();

            for (Map.Entry<M, Vertex<M>> link : refactoredModel.links().inverse().entrySet()) {
                Box box = inverse(diagram.links()).get(link.getKey());
                Vertex<Box> vertex = boxToVertex.get(box);
                expectedLinks.put(vertex, link.getValue());
            }

            MutableBiMap<Vertex<Box>, Vertex<M>> actualLinks = this.match(diagramGraph, refactoredModel.graph());

            Metrics metrics = MapMetrics.from(expectedLinks, actualLinks);
            modelStats.add(metrics, 1.0);
        }
    }

    private static <A, B> Map<B, A> inverse(Map<A, B> map) {
        Map<B, A> result = new IdentityHashMap<>();
        for (Map.Entry<A, B> entry : map.entrySet()) {
            if (result.put(entry.getValue(), entry.getKey()) != null) {
                throw new IllegalStateException("Duplicate value: " + entry.getValue());
            }
        }
        return result;
    }

    private <M> MutableBiMap<Vertex<Box>, Vertex<M>> match(DirectedMultigraph<Vertex<Box>, Edge> a, DirectedMultigraph<Vertex<M>, Edge> b) {
        SimilarityFloodingAlgorithm<Vertex<Box>, Vertex<M>, Label> algorithm = new SimilarityFloodingAlgorithm<>(
                SyntheticDiagramMatchingEvaluationTest.DEFAULT_EPSILON, 10000, PropagationCoefficientFormula.getInverseProductFormula(), FixpointFormula
                        .getBasicFormula());

        SimilarityMapping<Vertex<Box>, Vertex<M>> levenshtein = new SimilarityMapping<>(pair -> TextSimilarity.byLevenshtein(pair.getFirst().getName(), pair
                .getSecond()
                .getName()));

        SimilarityMapping<Vertex<Box>, Vertex<M>> mapping = algorithm.match(a, b, levenshtein);

        OrderedMatchingFilter<Vertex<Box>, Vertex<M>> filter = new OrderedMatchingFilter<>();
        return filter.filter(mapping, null);
    }

    private <M> MutableBiMap<Box, M> simplifyMatches(MutableBiMap<Vertex<Box>, Vertex<M>> matches) {
        MutableBiMap<Box, M> simplifiedMatches = new HashBiMap<>();
        for (Map.Entry<Vertex<Box>, Vertex<M>> match : matches.entrySet()) {
            simplifiedMatches.put(match.getKey().getRepresented(), match.getValue().getRepresented());
        }
        return simplifiedMatches;
    }
}
