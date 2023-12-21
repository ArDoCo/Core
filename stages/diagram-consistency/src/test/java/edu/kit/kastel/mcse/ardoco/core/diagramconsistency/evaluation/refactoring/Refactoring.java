/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.collections.impl.factory.Sets;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Edge;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Label;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * A refactoring is a change that can be applied to a graph. As it is not applied to the represented model, it
 * introduces an inconsistency between the graph and the model.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public abstract class Refactoring<R, M> {
    private final Random random = new Random();

    /**
     * Apply the refactoring to a graph.
     *
     * @param graph
     *              The graph. Will be modified.
     * @return True if the refactoring was applied, false otherwise.
     */
    public abstract boolean applyTo(AnnotatedGraph<R, M> graph);

    /**
     * Get a random number generator.
     *
     * @return The random number generator.
     */
    protected Random getRandom() {
        return this.random;
    }

    /**
     * Randomly select an entry from a collection.
     */
    protected <T> T selectEntry(Collection<T> elements, Predicate<T> filter) {
        List<T> filtered = elements.stream().filter(filter).toList();

        if (filtered.isEmpty()) {
            return null;
        }

        int index = this.getRandom().nextInt(filtered.size());
        return filtered.get(index);
    }

    /**
     * Randomly select an entry from a collection.
     */
    protected <T> T selectEntry(Collection<T> elements) {
        return this.selectEntry(elements, e -> true);
    }

    /**
     * Find the element that is linked to a given representative.
     */
    protected M findLinkedElement(Vertex<R> vertex, AnnotatedGraph<R, M> graph) {
        return graph.links().get(vertex);
    }

    protected Set<Vertex<R>> getAllChildren(Vertex<R> vertex, AnnotatedGraph<R, M> graph) {
        Set<Vertex<R>> children = Sets.mutable.empty();

        Queue<Vertex<R>> queue = new java.util.ArrayDeque<>();

        queue.add(vertex);

        while (!queue.isEmpty()) {
            Vertex<R> current = queue.poll();

            boolean added = children.add(current);
            if (!added) {
                continue;
            }

            for (Edge incoming : graph.graph().incomingEdgesOf(current)) {
                if (incoming.getLabel() == Label.HIERARCHY) {
                    queue.add(graph.graph().getEdgeSource(incoming));
                }
            }
        }

        return children;
    }
}
