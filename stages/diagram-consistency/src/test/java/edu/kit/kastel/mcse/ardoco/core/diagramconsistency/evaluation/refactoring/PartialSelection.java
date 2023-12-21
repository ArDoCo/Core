/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Label;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * This selects only a part of the model, discarding the rest and all links to it.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class PartialSelection<R, M> extends Refactoring<R, M> {
    private final double minRelativeSize;
    private final double maxRelativeSize;

    /**
     * Creates a new partial selection. If the selected part is not in the given size range, the refactoring fails.
     *
     * @param minRelativeSize
     *                        The minimum relative size of the sub selection.
     * @param maxRelativeSize
     *                        The maximum relative size of the sub selection.
     */
    public PartialSelection(double minRelativeSize, double maxRelativeSize) {
        this.minRelativeSize = minRelativeSize;
        this.maxRelativeSize = maxRelativeSize;
    }

    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        int size = graph.graph().vertexSet().size();
        int minSize = (int) (size * this.minRelativeSize);
        int maxSize = (int) (size * this.maxRelativeSize);

        if (minSize == maxSize || maxSize <= 1) {
            return false;
        }

        Vertex<R> current = this.selectEntry(graph.graph().vertexSet(), vertex -> this.isLeaf(graph, vertex));
        Set<Vertex<R>> part = Set.of();

        while (current != null) {
            part = this.getAllChildren(current, graph);
            part.add(current);

            if (part.size() > maxSize) {
                return false;
            }

            if (part.size() > minSize) {
                break;
            }

            current = this.selectEntry(graph.graph()
                    .outgoingEdgesOf(current)
                    .stream()
                    .filter(edge -> edge.getLabel().equals(Label.HIERARCHY))
                    .map(edge -> graph.graph().getEdgeTarget(edge))
                    .collect(Collectors.toList()), vertex -> true);
        }

        Set<Vertex<R>> selectedPart = part;
        graph.graph().removeAllVertices(graph.graph().vertexSet().stream().filter(vertex -> !selectedPart.contains(vertex)).collect(Collectors.toList()));

        graph.links().removeIf((vertex, element) -> !selectedPart.contains(vertex));
        graph.inconsistencies().removeIf(inconsistency -> !selectedPart.contains(inconsistency.getBox()));

        return true;
    }

    private boolean isLeaf(AnnotatedGraph<R, M> graph, Vertex<R> vertex) {
        return graph.graph().outgoingEdgesOf(vertex).stream().anyMatch(edge -> edge.getLabel().equals(Label.HIERARCHY)) && graph.graph()
                .incomingEdgesOf(vertex)
                .stream()
                .noneMatch(edge -> edge.getLabel().equals(Label.HIERARCHY));
    }
}
