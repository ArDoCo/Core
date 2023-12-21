/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.MissingBoxInconsistency;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * This refactoring deletes an element.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class Delete<R, M> extends Refactoring<R, M> {
    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        Vertex<R> vertexToDelete = this.selectEntry(graph.graph().vertexSet(), vertex -> this.isDeletingValid(graph, vertex));
        if (vertexToDelete == null) {
            return false;
        }

        graph.graph().removeVertex(vertexToDelete);

        graph.addInconsistency(new MissingBoxInconsistency<>(this.findLinkedElement(vertexToDelete, graph)));
        graph.links().removeIf((r, m) -> r.equals(vertexToDelete));

        graph.inconsistencies()
                .removeIf(inconsistency -> Objects.equals(inconsistency.getBox(), vertexToDelete) || Objects.equals(inconsistency.getOtherBox(),
                        vertexToDelete));

        return true;
    }

    private boolean isDeletingValid(AnnotatedGraph<R, M> graph, Vertex<R> vertexToDelete) {
        // Deleting a vertex with no links does not introduce a new inconsistency.
        if (this.findLinkedElement(vertexToDelete, graph) == null) {
            return false;
        }

        // Deleting a vertex that is already part of an inconsistency would destroy the existing inconsistency.
        boolean isPartOfInconsistency = graph.inconsistencies()
                .stream()
                .anyMatch(inconsistency -> inconsistency.getBox() != null && inconsistency.getBox().equals(vertexToDelete));

        return !isPartOfInconsistency;
    }
}
