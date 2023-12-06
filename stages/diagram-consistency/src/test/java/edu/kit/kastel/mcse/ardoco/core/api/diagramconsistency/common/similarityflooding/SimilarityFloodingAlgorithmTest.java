/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SimilarityFloodingAlgorithmTest {
    private static final double DELTA = 0.1;

    @DisplayName("Match with the basic example")
    @Test
    void basicExample() {
        // The example graphs and similarity values are taken from the work describing the similarity flooding algorithm.
        // There, images of the graphs and the steps of the algorithm can be found.
        // S. Melnik, H. Garcia-Molina, and E. Rahm, ‘Similarity flooding: a versatile graph matching algorithm and its application to schema matching’, in Proceedings 18th International Conference on Data Engineering, Feb. 2002, pp. 117–128. doi: 10.1109/ICDE.2002.994702.

        DirectedMultigraph<GraphA, Edge> a = this.buildGraphA();
        DirectedMultigraph<GraphB, Edge> b = this.buildGraphB();

        SimilarityFloodingAlgorithm<GraphA, GraphB, Label> algorithm = new SimilarityFloodingAlgorithm<>(0.075, 100, PropagationCoefficientFormula
                .getInverseProductFormula(), FixpointFormula.getBasicFormula());
        SimilarityMapping<GraphA, GraphB> mapping = algorithm.match(a, b, new SimilarityMapping<>(1.0));

        assertEquals(1.00, mapping.getSimilarity(GraphA.A, GraphB.B), DELTA);
        assertEquals(0.91, mapping.getSimilarity(GraphA.A2, GraphB.B1), DELTA);
        assertEquals(0.69, mapping.getSimilarity(GraphA.A1, GraphB.B2), DELTA);
        assertEquals(0.39, mapping.getSimilarity(GraphA.A1, GraphB.B1), DELTA);
        assertEquals(0.33, mapping.getSimilarity(GraphA.A1, GraphB.B), DELTA);
        assertEquals(0.33, mapping.getSimilarity(GraphA.A2, GraphB.B2), DELTA);

        OrderedMatchingFilter<GraphA, GraphB> filter = new OrderedMatchingFilter<>();
        MutableBiMap<GraphA, GraphB> finalMapping = filter.filter(mapping, null);

        assertEquals(3, finalMapping.size());
        assertEquals(GraphB.B, finalMapping.get(GraphA.A));
        assertEquals(GraphB.B1, finalMapping.get(GraphA.A2));
        assertEquals(GraphB.B2, finalMapping.get(GraphA.A1));
    }

    @DisplayName("Match a large graph with itself")
    @Test
    void largeGraph() {
        int size = 500;
        DirectedMultigraph<Integer, Edge> a = this.buildLargeGraph(size);
        DirectedMultigraph<Integer, Edge> b = this.buildLargeGraph(size);

        SimilarityFloodingAlgorithm<Integer, Integer, Label> algorithm = new SimilarityFloodingAlgorithm<>(0.0, size, PropagationCoefficientFormula
                .getInverseProductFormula(), FixpointFormula.getBasicFormula());
        SimilarityMapping<Integer, Integer> initialMapping = new SimilarityMapping<>(0.0);
        initialMapping.updateSimilarity(new Pair<>(0, 0), 1.0);
        SimilarityMapping<Integer, Integer> mapping = algorithm.match(a, b, initialMapping);

        OrderedMatchingFilter<Integer, Integer> filter = new OrderedMatchingFilter<>();
        MutableBiMap<Integer, Integer> finalMapping = filter.filter(mapping, null);

        assertEquals(size, finalMapping.size());
        for (int i = 0; i < size; i++) {
            assertEquals(i, finalMapping.get(i));
        }
    }

    private DirectedMultigraph<GraphA, Edge> buildGraphA() {
        DirectedMultigraph<GraphA, Edge> a = new DirectedMultigraph<>(Edge.class);

        a.addVertex(GraphA.A);
        a.addVertex(GraphA.A1);
        a.addVertex(GraphA.A2);

        a.addEdge(GraphA.A, GraphA.A1, new Edge(Label.L1));
        a.addEdge(GraphA.A, GraphA.A2, new Edge(Label.L1));
        a.addEdge(GraphA.A1, GraphA.A2, new Edge(Label.L2));

        return a;
    }

    private DirectedMultigraph<GraphB, Edge> buildGraphB() {
        DirectedMultigraph<GraphB, Edge> b = new DirectedMultigraph<>(Edge.class);

        b.addVertex(GraphB.B);
        b.addVertex(GraphB.B1);
        b.addVertex(GraphB.B2);

        b.addEdge(GraphB.B, GraphB.B1, new Edge(Label.L1));
        b.addEdge(GraphB.B, GraphB.B2, new Edge(Label.L2)); // Note the different label of the edge.
        b.addEdge(GraphB.B2, GraphB.B1, new Edge(Label.L2)); // Note the different direction of the edge.

        return b;
    }

    private DirectedMultigraph<Integer, Edge> buildLargeGraph(int size) {
        DirectedMultigraph<Integer, Edge> graph = new DirectedMultigraph<>(Edge.class);

        for (int i = 0; i < size; i++) {
            graph.addVertex(i);
        }

        for (int i = 0; i < size - 1; i++) {
            graph.addEdge(i, i + 1, new Edge(Label.L1));

        }

        return graph;
    }

    enum Label {
        L1, L2
    }

    enum GraphA {
        A, A1, A2
    }

    enum GraphB {
        B, B1, B2
    }

    static final class Edge extends DefaultEdge implements LabeledEdge<Label> {

        private final Label label;

        public Edge(Label label) {
            this.label = label;
        }

        @Override
        public Label getLabel() {
            return this.label;
        }
    }
}
