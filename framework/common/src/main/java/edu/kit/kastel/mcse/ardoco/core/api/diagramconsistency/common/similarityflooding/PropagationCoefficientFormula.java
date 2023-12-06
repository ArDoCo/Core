/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding;

import java.util.Set;

import org.jgrapht.alg.util.Pair;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Describes how to compute the propagation coefficient for a vertex.
 * The formulas do not use the order of the vertices, which ensures that even with non-deterministic orderings the result
 * is the deterministic.
 *
 * @param <A>
 *            The type of the vertices in the first graph.
 * @param <B>
 *            The type of the vertices in the second graph.
 */
@FunctionalInterface
@Deterministic
public interface PropagationCoefficientFormula<A, B> {
    /**
     * Calculate the inverse average.
     *
     * @param <A>
     *            The type of the vertices in the first graph.
     * @param <B>
     *            The type of the vertices in the second graph.
     * @return The inverse average formula.
     */
    static <A, B> PropagationCoefficientFormula<A, B> getInverseAverageFormula() {
        return vertices -> 2.0 / (vertices.getFirst().size() + vertices.getSecond().size());
    }

    /**
     * Calculate the inverse product.
     *
     * @param <A>
     *            The type of the vertices in the first graph.
     * @param <B>
     *            The type of the vertices in the second graph.
     * @return The inverse average formula.
     */
    static <A, B> PropagationCoefficientFormula<A, B> getInverseProductFormula() {
        return vertices -> {
            if (vertices.getFirst().isEmpty() || vertices.getSecond().isEmpty()) {
                return 0.0;
            }

            return 1.0 / (vertices.getFirst().size() * vertices.getSecond().size());
        };
    }

    /**
     * Calculates the propagation coefficient for a vertex and a specific label and edge category (in/out).
     *
     * @param vertices
     *                 The neighboring vertices along an edge with a specific label and edge category.
     * @return The propagation coefficient.
     */
    double calculate(Pair<Set<A>, Set<B>> vertices);
}
