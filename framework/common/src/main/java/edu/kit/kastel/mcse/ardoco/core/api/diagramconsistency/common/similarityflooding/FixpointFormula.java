/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * The formula for the fixpoint computation.
 * The four formulas defined here are described first in: S. Melnik, H. Garcia-Molina, and E. Rahm, ‘Similarity
 * flooding: a versatile graph matching algorithm and its application to schema matching’, in Proceedings 18th
 * International Conference on Data Engineering, Feb. 2002, pp. 117–128. doi: 10.1109/ICDE.2002.994702.
 */
public interface FixpointFormula {
    /**
     * Add the three given mappings together.
     *
     * @param first  The first mapping.
     * @param second The second mapping.
     * @param third  The third mapping.
     * @return The sum of the mappings.
     */
    static List<Double> add(List<Double> first, List<Double> second, List<Double> third) {
        return add(add(first, second), third);
    }

    /**
     * Add the two given mappings together.
     *
     * @param first
     *               The first mapping.
     * @param second
     *               The second mapping.
     * @return The sum of the mappings.
     */
    static List<Double> add(List<Double> first, List<Double> second) {
        if (first.size() != second.size()) {
            throw new IllegalArgumentException("The given mappings must have the same size.");
        }

        List<Double> result = new ArrayList<>(first.size());
        for (int i = 0; i < first.size(); i++) {
            result.add(first.get(i) + second.get(i));
        }

        return result;
    }

    /**
     * Normalize the given mapping.
     *
     * @param mapping
     *                The mapping to normalize.
     * @return The normalized mapping.
     */
    static List<Double> normalize(List<Double> mapping) {
        double max = mapping.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        if (max == 0) {
            return mapping;
        }

        return mapping.stream().map(value -> value / max).toList();
    }

    /**
     * Get the basic formula. {@code sigma[i+1] = normalize(sigma[i] + flood(sigma[i]))}
     * This formula is used for the basic examples in the original paper but is not the recommended formula.
     * Consider using {@link #getCFormula()} instead.
     *
     * @return The basic formula.
     */
    static FixpointFormula getBasicFormula() {
        return (initialMapping, previousMapping, flood) -> FixpointFormula.normalize(FixpointFormula.add(previousMapping, flood.apply(previousMapping)));
    }

    /**
     * Get the A formula. {@code sigma[i+1] = normalize(sigma[0] + flood(sigma[i]))}
     * This formula is named "A" in the original paper.
     *
     * @return The basic formula.
     */
    static FixpointFormula getAFormula() {
        return (initialMapping, previousMapping, flood) -> FixpointFormula.normalize(FixpointFormula.add(initialMapping, flood.apply(previousMapping)));
    }

    /**
     * Get the B formula. {@code sigma[i+1] = normalize(sigma[0] + sigma[i] + flood(sigma[i]))}
     * This formula is named "B" in the original paper.
     *
     * @return The basic formula.
     */
    static FixpointFormula getBFormula() {
        return (initialMapping, previousMapping, flood) -> FixpointFormula.normalize(flood.apply(FixpointFormula.add(initialMapping, previousMapping)));
    }

    /**
     * Get the C formula. {@code sigma[i+1] = normalize(sigma[0] + sigma[i] + flood(sigma[0] + sigma[i]))}
     * This formula is named "C" in the original paper, and is the recommended formula.
     *
     * @return The basic formula.
     */
    static FixpointFormula getCFormula() {
        return (initialMapping, previousMapping, flood) -> FixpointFormula.normalize(FixpointFormula.add(initialMapping, previousMapping, flood.apply(
                FixpointFormula.add(initialMapping, previousMapping))));
    }

    /**
     * Calculate the next mapping based on the previous mapping.
     *
     * @param initialMapping
     *                        The initial mapping.
     * @param previousMapping
     *                        The previous mapping.
     * @param flood
     *                        The flooding function.
     * @return The next mapping.
     */
    List<Double> calculate(List<Double> initialMapping, List<Double> previousMapping, UnaryOperator<List<Double>> flood);
}
