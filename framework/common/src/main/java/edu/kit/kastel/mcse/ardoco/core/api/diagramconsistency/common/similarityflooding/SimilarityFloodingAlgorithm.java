/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.collections.impl.factory.Sets;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Similarity flooding is a graph matching algorithm presented in: S. Melnik, H. Garcia-Molina, and E. Rahm, ‘Similarity
 * flooding: a versatile graph matching algorithm and its application to schema matching’, in Proceedings 18th
 * International Conference on Data Engineering, Feb. 2002, pp. 117–128. doi: 10.1109/ICDE.2002.994702.
 *
 * @param <A>
 *            The type of the vertices of the first graph.
 * @param <B>
 *            The type of the vertices of the second graph.
 * @param <L>
 *            The edge label type.
 */
@Deterministic
public class SimilarityFloodingAlgorithm<A, B, L> {
    private final double epsilon;
    private final int maxIterations;
    private final PropagationCoefficientFormula<A, B> propagationCoefficientFormula;
    private final FixpointFormula fixpointFormula;

    /**
     * Create a new instance of the SimilarityFloodingAlgorithm.
     *
     * @param epsilon
     *                                      The threshold at which the algorithm stops.
     * @param maxIterations
     *                                      The maximum number of iterations the algorithm should perform, even if the threshold is not reached.
     * @param propagationCoefficientFormula
     *                                      The equation used to calculate the propagation coefficient.
     * @param fixpointFormula
     *                                      The equation used to calculate the next iteration's similarity mapping.
     */
    public SimilarityFloodingAlgorithm(double epsilon, int maxIterations, PropagationCoefficientFormula<A, B> propagationCoefficientFormula,
            FixpointFormula fixpointFormula) {
        if (maxIterations < 0) {
            throw new IllegalArgumentException("maxIterations cannot be negative");
        }

        this.epsilon = epsilon;
        this.maxIterations = maxIterations;
        this.propagationCoefficientFormula = propagationCoefficientFormula;
        this.fixpointFormula = fixpointFormula;
    }

    /**
     * Match two graphs a and b, searching for similar vertices.
     * This executes the similarity flooding algorithm.
     *
     * @param a
     *                                The first graph.
     * @param b
     *                                The second graph.
     * @param initialSimilarityValues
     *                                The initial similarities of the vertices.
     * @param <E>
     *                                The actual edge type, proving the label of type {@link L}.
     * @return The mapping between the vertices of the two graphs.
     */
    public <E extends LabeledEdge<L>> SimilarityMapping<A, B> match(DirectedMultigraph<A, E> a, DirectedMultigraph<B, E> b,
            SimilarityMapping<A, B> initialSimilarityValues) {
        DirectedWeightedMultigraph<Pair<A, B>, Edge> basePropagationGraph = this.buildPropagationGraph(a, b);

        List<Pair<A, B>> vertices = new ArrayList<>(basePropagationGraph.vertexSet());
        DirectedWeightedMultigraph<Integer, Edge> workingPropagationGraph = this.changeVertexType(vertices, basePropagationGraph);

        // The SimilarityMapping can be represented by the function (a, b) -> double.
        // To reduce overhead of searching in maps, list are used instead.
        // The list provides the operation int -> double.
        // The method changeVertexType determines the mapping (a, b) -> int.
        // With these two new mappings, the same functionality is achieved.

        List<Double> initialMapping = initialSimilarityValues.getMappedValues(vertices);
        List<Double> previousMapping = initialMapping;
        List<Double> nextMapping = initialMapping;

        double delta = Double.POSITIVE_INFINITY;
        for (int iteration = 0; iteration < this.maxIterations && delta > this.epsilon; iteration++) {
            nextMapping = this.fixpointFormula.calculate(initialMapping, previousMapping, mapping -> this.floodSimilarities(workingPropagationGraph, mapping));
            delta = this.residuum(previousMapping, nextMapping);

            previousMapping = nextMapping;
        }

        return new SimilarityMapping<>(vertices, nextMapping);
    }

    private <V, E extends LabeledEdge<L>> Map<L, Set<V>> getInNeighbors(DirectedMultigraph<V, E> graph, V vertex) {
        return graph.incomingEdgesOf(vertex)
                .stream()
                .collect(Collectors.groupingBy(LabeledEdge::getLabel, Collectors.mapping(graph::getEdgeSource, Collectors.toCollection(LinkedHashSet::new))));
    }

    private <V, E extends LabeledEdge<L>> Map<L, Set<V>> getOutNeighbors(DirectedMultigraph<V, E> graph, V vertex) {
        return graph.outgoingEdgesOf(vertex)
                .stream()
                .collect(Collectors.groupingBy(LabeledEdge::getLabel, Collectors.mapping(graph::getEdgeTarget, Collectors.toCollection(LinkedHashSet::new))));
    }

    private <E extends LabeledEdge<L>> Map<L, Pair<Set<A>, Set<B>>> getPairwiseNeighbors(DirectedMultigraph<A, E> a, DirectedMultigraph<B, E> b,
            BiFunction<DirectedMultigraph<A, E>, A, Map<L, Set<A>>> neighborInAProvider,
            BiFunction<DirectedMultigraph<B, E>, B, Map<L, Set<B>>> neighborInBProvider, Pair<A, B> vertex) {
        Map<L, Set<A>> neighborsInGraphA = neighborInAProvider.apply(a, vertex.getFirst());
        Map<L, Set<B>> neighborsInGraphB = neighborInBProvider.apply(b, vertex.getSecond());

        return Sets.union(neighborsInGraphA.keySet(), neighborsInGraphB.keySet())
                .toMap(label -> label, label -> Pair.of(neighborsInGraphA.getOrDefault(label, Set.of()), neighborsInGraphB.getOrDefault(label, Set.of())));
    }

    private <E extends LabeledEdge<L>> Map<L, Pair<Set<A>, Set<B>>> getInNeighbors(DirectedMultigraph<A, E> a, DirectedMultigraph<B, E> b, Pair<A, B> vertex) {
        return this.getPairwiseNeighbors(a, b, this::getInNeighbors, this::getInNeighbors, vertex);
    }

    private <E extends LabeledEdge<L>> Map<L, Pair<Set<A>, Set<B>>> getOutNeighbors(DirectedMultigraph<A, E> a, DirectedMultigraph<B, E> b, Pair<A, B> vertex) {
        return this.getPairwiseNeighbors(a, b, this::getOutNeighbors, this::getOutNeighbors, vertex);
    }

    /**
     * Calculate the propagation coefficients for each label. If the coefficient is 0.0, the label is filtered out. This
     * is deterministic as the order of the labels is not used by the formula.
     */
    private Map<L, Double> calculateFilteredCoefficients(Map<L, Pair<Set<A>, Set<B>>> verticesByLabel) {
        return verticesByLabel.entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), this.propagationCoefficientFormula.calculate(entry.getValue())))
                .filter(entry -> entry.getValue() > 0.0)
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    /**
     * Add edges to all neighbors given by the cartesian product of components in 'neighbors'.
     */
    private void addEdgesToNeighbors(Graph<Pair<A, B>, Edge> graph, Pair<A, B> vertex, Pair<Set<A>, Set<B>> neighbors, double coefficient) {
        var combinedNeighbors = Sets.cartesianProduct(neighbors.getFirst(), neighbors.getSecond(), Pair::new);
        for (Pair<A, B> neighbor : combinedNeighbors) {
            graph.addVertex(neighbor);

            Edge edge = graph.addEdge(vertex, neighbor);
            graph.setEdgeWeight(edge, coefficient);
        }
    }

    /**
     * For a given vertex, add edges to all neighbors given by the cartesian product of components in 'neighborsByLabel'.
     * The edge weights are determined by 'coefficientsByLabel'.
     */
    private void addEdgesToAllNeighbors(Graph<Pair<A, B>, Edge> graph, Pair<A, B> vertex, Map<L, Pair<Set<A>, Set<B>>> neighborsByLabel,
            Map<L, Double> coefficientsByLabel) {
        for (var entry : coefficientsByLabel.entrySet()) {
            Pair<Set<A>, Set<B>> currentNeighborGroup = neighborsByLabel.get(entry.getKey());
            this.addEdgesToNeighbors(graph, vertex, currentNeighborGroup, entry.getValue());
        }
    }

    /**
     * Build the propagation graph from the two given graphs.
     * The propagation graph uses the cartesian product of the vertices of the two graphs as vertices.
     * The edges are weighted according to the propagation coefficient formula.
     * If an edge as weight 0.0, it is not added to the graph.
     * If a vertex has no incoming or outgoing edges, it is not added to the graph.
     */
    private <E extends LabeledEdge<L>> DirectedWeightedMultigraph<Pair<A, B>, Edge> buildPropagationGraph(DirectedMultigraph<A, E> a,
            DirectedMultigraph<B, E> b) {
        DirectedWeightedMultigraph<Pair<A, B>, Edge> propagation = new DirectedWeightedMultigraph<>(null, Edge::new);

        Set<Pair<A, B>> possibleVertices = Sets.cartesianProduct(a.vertexSet(), b.vertexSet(), Pair::new).toSet();
        Set<Pair<A, B>> requiredVertices = new LinkedHashSet<>();

        for (Pair<A, B> vertex : possibleVertices) {
            Map<L, Pair<Set<A>, Set<B>>> inNeighbors = this.getInNeighbors(a, b, vertex);
            Map<L, Double> inCoefficients = this.calculateFilteredCoefficients(inNeighbors);

            Map<L, Pair<Set<A>, Set<B>>> outNeighbors = this.getOutNeighbors(a, b, vertex);
            Map<L, Double> outCoefficients = this.calculateFilteredCoefficients(outNeighbors);

            if (inCoefficients.isEmpty() && outCoefficients.isEmpty()) {
                continue;
            }

            propagation.addVertex(vertex);
            requiredVertices.add(vertex);

            this.addEdgesToAllNeighbors(propagation, vertex, inNeighbors, inCoefficients);
            this.addEdgesToAllNeighbors(propagation, vertex, outNeighbors, outCoefficients);
        }

        propagation.removeAllVertices(Sets.difference(possibleVertices, requiredVertices).toSet());

        return propagation;
    }

    /**
     * Change the vertex type of the propagation graph from {@link Pair} to {@link Integer}.
     * This is done to improve performance, as hashing and comparing integers is faster.
     * The integers are indices into the mapping list.
     */
    private DirectedWeightedMultigraph<Integer, Edge> changeVertexType(List<Pair<A, B>> vertices,
            DirectedWeightedMultigraph<Pair<A, B>, Edge> propagationGraph) {
        Map<Pair<A, B>, Integer> vertexMapping = IntStream.range(0, vertices.size()).boxed().collect(Collectors.toMap(vertices::get, Function.identity()));

        DirectedWeightedMultigraph<Integer, Edge> newGraph = new DirectedWeightedMultigraph<>(null, Edge::new);

        for (Pair<A, B> vertex : propagationGraph.vertexSet()) {
            newGraph.addVertex(vertexMapping.get(vertex));
        }

        for (Edge edge : propagationGraph.edgeSet()) {
            Pair<A, B> source = propagationGraph.getEdgeSource(edge);
            Pair<A, B> target = propagationGraph.getEdgeTarget(edge);

            Edge newEdge = newGraph.addEdge(vertexMapping.get(source), vertexMapping.get(target));
            newGraph.setEdgeWeight(newEdge, propagationGraph.getEdgeWeight(edge));
        }

        return newGraph;
    }

    /**
     * Propagate the similarities of each vertex along the edges of the propagation graph.
     * The edge weights are used as coefficients.
     * This method is deterministic as addition is commutative.
     */
    private List<Double> floodSimilarities(DirectedWeightedMultigraph<Integer, Edge> propagationGraph, List<Double> previousMapping) {
        List<Double> nextMapping = new ArrayList<>(previousMapping.size());

        for (int vertex = 0; vertex < previousMapping.size(); vertex++) {
            Set<Edge> edges = propagationGraph.incomingEdgesOf(vertex);

            double similarity = 0.0;

            for (Edge edge : edges) {
                int source = propagationGraph.getEdgeSource(edge);
                double coefficient = propagationGraph.getEdgeWeight(edge);

                similarity += coefficient * previousMapping.get(source);
            }

            nextMapping.add(similarity);
        }

        return nextMapping;
    }

    /**
     * The Euclidean distance of the two mappings, interpreted as vectors.
     */
    private double residuum(List<Double> previousMapping, List<Double> nextMapping) {
        double residuum = 0.0;

        for (int i = 0; i < previousMapping.size(); i++) {
            double delta = Math.abs(previousMapping.get(i) - nextMapping.get(i));
            residuum += delta * delta;
        }

        return Math.sqrt(residuum);
    }

    private static final class Edge extends DefaultWeightedEdge {
    }
}
