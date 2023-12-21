/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * This selects only the package part of a diagram, determined by naming.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class PackageSelection<R, M> extends Refactoring<R, M> {

    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        Set<Vertex<R>> toRemove = new LinkedHashSet<>();

        for (Vertex<R> vertex : graph.graph().vertexSet()) {
            if (!vertex.getName().isEmpty() && !Character.isLowerCase(vertex.getName().charAt(0))) {
                toRemove.add(vertex);
            }
        }

        graph.graph().removeAllVertices(toRemove);
        graph.links().removeIf((r, m) -> toRemove.contains(r));

        return true;
    }
}
