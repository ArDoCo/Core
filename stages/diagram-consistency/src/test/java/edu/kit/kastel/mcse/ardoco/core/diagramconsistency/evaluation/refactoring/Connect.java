/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Edge;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Label;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.InconsistencyType;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.UnexpectedLineInconsistency;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * This refactoring connects two elements with a line.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class Connect<R, M> extends Refactoring<R, M> {
    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        Vertex<R> sourceToConnect = this.selectEntry(graph.graph().vertexSet(), v -> this.isConnectingValid(graph, v));
        if (sourceToConnect == null) {
            return false;
        }

        Set<Vertex<R>> invalidTargets = graph.graph()
                .outgoingEdgesOf(sourceToConnect)
                .stream()
                .map(edge -> graph.graph().getEdgeTarget(edge))
                .collect(Collectors.toSet());
        invalidTargets.add(sourceToConnect);

        Vertex<R> targetToConnect = this.selectEntry(graph.graph().vertexSet(), v -> !invalidTargets.contains(v));
        if (targetToConnect == null) {
            return false;
        }

        graph.graph().addEdge(sourceToConnect, targetToConnect, new Edge(Label.DEFAULT));

        graph.addInconsistency(new UnexpectedLineInconsistency<>(sourceToConnect, targetToConnect));

        return true;
    }

    private boolean isConnectingValid(AnnotatedGraph<R, M> graph, Vertex<R> vertexToConnect) {
        // Connecting a vertex with no links does not introduce a new inconsistency.
        if (this.findLinkedElement(vertexToConnect, graph) == null) {
            return false;
        }

        // Connecting a vertex that is missing a line might repair that inconsistency.
        boolean isMissingLine = graph.inconsistencies()
                .stream()
                .anyMatch(inconsistency -> inconsistency.getBox() != null && inconsistency.getBox().equals(vertexToConnect) && inconsistency
                        .getType() == InconsistencyType.MISSING_LINE);

        return !isMissingLine;
    }
}
