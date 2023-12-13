/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Edge;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Label;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.UnexpectedBoxInconsistency;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;

/**
 * This refactoring creates a new element.
 *
 * @param <R>
 *            The type of the representatives that represent the model elements, e.g. boxes in a diagram.
 * @param <M>
 *            The type of the model elements.
 */
public class Create<R, M> extends Refactoring<R, M> {
    @Override
    public boolean applyTo(AnnotatedGraph<R, M> graph) {
        Vertex<R> createdVertex = new Vertex<>(null, RandomStringUtils.randomAscii(8));
        graph.graph().addVertex(createdVertex);

        graph.addInconsistency(new UnexpectedBoxInconsistency<>(createdVertex));

        Vertex<R> parentVertex = null;
        if (this.getRandom().nextBoolean()) {
            parentVertex = this.selectEntry(graph.graph().vertexSet());
            if (parentVertex != null && !parentVertex.equals(createdVertex)) {
                graph.graph().addEdge(createdVertex, parentVertex, new Edge(Label.HIERARCHY));
            }
        }

        int newEdgeBudget = this.getRandom().nextInt(4);
        if (newEdgeBudget > 0) {
            int incomingEdgeBudget = this.getRandom().nextInt(newEdgeBudget);
            int outgoingEdgeBudget = newEdgeBudget - incomingEdgeBudget;

            Set<Vertex<R>> blackList = new LinkedHashSet<>();
            blackList.add(createdVertex);

            if (parentVertex != null) {
                blackList.add(parentVertex);
            }

            for (int i = 0; i < incomingEdgeBudget; i++) {
                Vertex<R> sourceVertex = this.selectEntry(graph.graph().vertexSet(), vertex -> !blackList.contains(vertex));
                if (sourceVertex != null) {
                    graph.graph().addEdge(sourceVertex, createdVertex, new Edge(Label.DEFAULT));
                    blackList.add(sourceVertex);
                }
            }

            for (int i = 0; i < outgoingEdgeBudget; i++) {
                Vertex<R> targetVertex = this.selectEntry(graph.graph().vertexSet(), vertex -> !blackList.contains(vertex));
                if (targetVertex != null) {
                    graph.graph().addEdge(createdVertex, targetVertex, new Edge(Label.DEFAULT));
                    blackList.add(targetVertex);
                }
            }
        }

        return true;
    }
}
