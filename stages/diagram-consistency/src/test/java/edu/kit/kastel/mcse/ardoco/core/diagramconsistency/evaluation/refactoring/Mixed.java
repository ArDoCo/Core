/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * This refactoring applies the standard refactorings until a total count relative to the model size is reached.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class Mixed<R, M> extends Refactoring<R, M> {
    private final double ratio;
    private final Refactoring<R, M> preRefactoring;
    private final List<Refactoring<R, M>> refactorings = List.of(new Connect<>(), new Create<>(), new Delete<>(), new Disconnect<>(), new Move<>(),
            new Rename<>());

    /**
     * Creates a new mixed refactoring.
     *
     * @param ratio
     *                       The ratio of the refactoring count to the model size.
     * @param preRefactoring
     *                       The refactoring that is applied before the standard refactorings, optional.
     */
    public Mixed(double ratio, Refactoring<R, M> preRefactoring) {
        this.ratio = ratio;
        this.preRefactoring = preRefactoring;
    }

    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        if (this.preRefactoring != null) {
            boolean success = this.preRefactoring.applyTo(graph);
            if (!success) {
                return false;
            }
        }

        int refactoringCount = (int) (graph.graph().vertexSet().size() * this.ratio);
        for (int i = 0; i < refactoringCount; i++) {
            Refactoring<R, M> refactoring = this.selectEntry(this.refactorings);
            if (refactoring == null) {
                return false;
            }

            boolean success = refactoring.applyTo(graph);
            if (!success) {
                return false;
            }
        }

        return true;
    }
}
