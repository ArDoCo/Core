package edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results_new;

import org.eclipse.collections.api.list.ImmutableList;

public record ResultMatrix<T> (ImmutableList<T> truePositives, int trueNegatives,
                               ImmutableList<T> falsePositives, ImmutableList<T> falseNegatives) {
}
