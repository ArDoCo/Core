/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * This refactoring appends a suffix to all model elements. The change is not classified as an inconsistency to not
 * prevent renaming.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class AppendSuffix<R, M> extends Refactoring<R, M> {
    private final String suffix;

    /**
     * Creates a new suffix appending refactoring.
     *
     * @param suffix
     *               The suffix to append.
     */
    public AppendSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        for (Vertex<R> vertex : graph.graph().vertexSet()) {
            vertex.rename(vertex.getName() + this.suffix);
        }

        return true;
    }
}
