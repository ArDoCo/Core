/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.eval.results;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * reprensents the results in the form of a matrix
 * 
 * @param truePositives  the true positives
 * @param trueNegatives  the true negatives
 * @param falsePositives the false positives
 * @param falseNegatives the false negatives
 */
public record ResultMatrix<T>(ImmutableList<T> truePositives, int trueNegatives, ImmutableList<T> falsePositives, ImmutableList<T> falseNegatives) {
}
