/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Edge;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Label;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.HierarchyInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.InconsistencyType;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * This refactoring moves an element in the hierarchy.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class Move<R, M> extends Refactoring<R, M> {

    private static <R, M> Vertex<R> getParent(AnnotatedGraph<R, M> graph, Vertex<R> vertexToMove) {
        return graph.graph()
                .outgoingEdgesOf(vertexToMove)
                .stream()
                .filter(edge -> edge.getLabel() == Label.HIERARCHY)
                .map(graph.graph()::getEdgeTarget)
                .findFirst()
                .orElse(null);
    }

    private static <R, M> void attachToParent(AnnotatedGraph<R, M> graph, Vertex<R> vertexToMove, Vertex<R> newParentVertex) {
        graph.graph().addEdge(vertexToMove, newParentVertex, new Edge(Label.HIERARCHY));
    }

    private static <R, M> void detachFromParent(AnnotatedGraph<R, M> graph, Vertex<R> vertexToMove) {
        graph.graph()
                .removeAllEdges(graph.graph()
                        .outgoingEdgesOf(vertexToMove)
                        .stream()
                        .filter(edge -> edge.getLabel() == Label.HIERARCHY)
                        .collect(Collectors.toSet()));
    }

    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        Vertex<R> vertexToMove = this.selectEntry(graph.graph().vertexSet(), vertex -> this.isMovingValid(graph, vertex));
        if (vertexToMove == null) {
            return false;
        }

        Set<Vertex<R>> invalidTargets = this.getAllChildren(vertexToMove, graph);
        invalidTargets.add(vertexToMove);

        Vertex<R> newParentVertex = this.selectEntry(graph.graph().vertexSet(), vertex -> !invalidTargets.contains(vertex));
        if (newParentVertex == null) {
            return false;
        }

        Vertex<R> oldParentVertex = getParent(graph, vertexToMove);

        detachFromParent(graph, vertexToMove);
        attachToParent(graph, vertexToMove, newParentVertex);

        graph.addInconsistency(new HierarchyInconsistency<>(vertexToMove, this.findLinkedElement(vertexToMove, graph), oldParentVertex));

        return true;
    }

    private boolean isMovingValid(AnnotatedGraph<R, M> graph, Vertex<R> vertexToMove) {
        // Moving a vertex that is not linked does not introduce a new inconsistency.
        if (this.findLinkedElement(vertexToMove, graph) == null) {
            return false;
        }

        // Moving a vertex that is already moved does not introduce a new inconsistency.
        boolean alreadyMoved = graph.inconsistencies()
                .stream()
                .anyMatch(inconsistency -> inconsistency.getBox() != null && inconsistency.getBox().equals(vertexToMove) && inconsistency
                        .getType() == InconsistencyType.HIERARCHY_INCONSISTENCY);

        return !alreadyMoved;
    }
}
