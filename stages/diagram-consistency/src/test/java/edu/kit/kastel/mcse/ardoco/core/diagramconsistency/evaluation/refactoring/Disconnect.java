/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Label;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.InconsistencyType;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.MissingLineInconsistency;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * This refactoring removes a line between two elements.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class Disconnect<R, M> extends Refactoring<R, M> {
    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        Vertex<R> vertexToDisconnect = this.selectEntry(graph.graph().vertexSet(), vertex -> this.isDisconnectingValid(graph, vertex));
        if (vertexToDisconnect == null) {
            return false;
        }

        Vertex<R> targetVertex = this.selectEntry(graph.graph()
                .outgoingEdgesOf(vertexToDisconnect)
                .stream()
                .filter(edge -> edge.getLabel().equals(Label.DEFAULT))
                .map(edge -> graph.graph().getEdgeTarget(edge))
                .toList());
        if (targetVertex == null) {
            return false;
        }

        graph.graph().removeEdge(vertexToDisconnect, targetVertex);

        graph.addInconsistency(new MissingLineInconsistency<>(vertexToDisconnect, targetVertex));

        return true;
    }

    private boolean isDisconnectingValid(AnnotatedGraph<R, M> graph, Vertex<R> vertexToDisconnect) {
        // Disconnecting a vertex with no links does not introduce a new inconsistency.
        if (this.findLinkedElement(vertexToDisconnect, graph) == null) {
            return false;
        }

        // Disconnecting is not possible if there are no outgoing lines.
        if (graph.graph().outgoingEdgesOf(vertexToDisconnect).stream().noneMatch(edge -> edge.getLabel().equals(Label.DEFAULT))) {
            return false;
        }

        // Disconnecting a vertex that is unexpectedly connected might repair that inconsistency.
        boolean isUnexpectedlyConnected = graph.inconsistencies()
                .stream()
                .anyMatch(inconsistency -> inconsistency.getBox() != null && inconsistency.getBox().equals(vertexToDisconnect) && inconsistency
                        .getType() == InconsistencyType.UNEXPECTED_LINE);

        return !isUnexpectedlyConnected;
    }
}
