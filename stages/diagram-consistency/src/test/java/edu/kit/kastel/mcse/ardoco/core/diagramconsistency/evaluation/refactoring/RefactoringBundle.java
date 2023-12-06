/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * A group of refactorings that are applied together.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class RefactoringBundle<R, M> extends Refactoring<R, M> {
    private final Map<Refactoring<R, M>, Integer> refactorings;

    /**
     * Creates a new refactoring bundle.
     *
     * @param refactorings
     *                     The refactorings to apply, and the number of times to apply them.
     */
    public RefactoringBundle(Map<Refactoring<R, M>, Integer> refactorings) {
        this.refactorings = refactorings;
    }

    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        List<Refactoring<R, M>> refactoringPlan = new ArrayList<>();
        for (Map.Entry<Refactoring<R, M>, Integer> entry : this.refactorings.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                refactoringPlan.add(entry.getKey());
            }
        }

        Collections.shuffle(refactoringPlan, this.getRandom());

        for (Refactoring<R, M> refactoring : refactoringPlan) {
            boolean success = refactoring.applyTo(graph);
            if (!success) {
                return false;
            }
        }

        return true;
    }
}
