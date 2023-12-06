/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import org.apache.commons.lang3.RandomStringUtils;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.InconsistencyType;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.NameInconsistency;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * This refactoring renames a graph element.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class Rename<R, M> extends Refactoring<R, M> {
    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        Vertex<R> vertexToRename = this.selectEntry(graph.graph().vertexSet(), vertex -> this.isRenamingValid(graph, vertex));
        if (vertexToRename == null) {
            return false;
        }

        String oldName = vertexToRename.getName();
        String newName = RandomStringUtils.randomAscii(8);

        vertexToRename.rename(newName);

        graph.addInconsistency(new NameInconsistency<>(vertexToRename, this.findLinkedElement(vertexToRename, graph), oldName, newName));

        return true;
    }

    private boolean isRenamingValid(AnnotatedGraph<R, M> graph, Vertex<R> vertexToRename) {
        // Renaming a vertex that is not linked does not introduce a new inconsistency.
        if (this.findLinkedElement(vertexToRename, graph) == null) {
            return false;
        }

        // Renaming a vertex that is already renamed does not introduce a new inconsistency.
        boolean alreadyRenamed = graph.inconsistencies()
                .stream()
                .anyMatch(inconsistency -> inconsistency.getBox() != null && inconsistency.getBox().equals(vertexToRename) && inconsistency
                        .getType() == InconsistencyType.NAME_INCONSISTENCY);

        return !alreadyRenamed;
    }
}
