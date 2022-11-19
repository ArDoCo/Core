package edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new;

import org.eclipse.collections.api.list.ImmutableList;

public record ExplicitResultMatrix <T> (ImmutableList<T> truePositives, ImmutableList<T> trueNegatives,
                                    ImmutableList<T> falsePositives, ImmutableList<T> falseNegatives){

    public ResultMatrix getSimpleMatrix() {
        return new ResultMatrix(truePositives.size(), trueNegatives.size(), falsePositives.size(), falseNegatives.size());
    }
}
