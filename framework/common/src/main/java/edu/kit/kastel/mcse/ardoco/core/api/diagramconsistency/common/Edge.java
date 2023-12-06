/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

import org.jgrapht.graph.DefaultEdge;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.LabeledEdge;

/**
 * The edge type used in the graphs that are matched.
 */
public class Edge extends DefaultEdge implements LabeledEdge<Edge.Label> {
    private final Label label;

    /**
     * Creates a new edge with the given label.
     *
     * @param label
     *              The label of the edge.
     */
    public Edge(Label label) {
        this.label = label;
    }

    @Override
    public Label getLabel() {
        return this.label;
    }

    /**
     * Performs a copy of the edge.
     *
     * @return The copy.
     */
    public Edge copy() {
        return new Edge(this.label);
    }

    /**
     * Edge labels used in the matched graphs representing diagrams and models.
     */
    public enum Label {
        /**
         * The edge is a normal edge, representing either a line in a diagram or a normal relation like dependency in a
         * model.
         */
        DEFAULT,
        /**
         * The edge represents a hierarchy relation. It is used for 'inside'-relations in diagrams, thus the source is
         * lower than the target in the hierarchy.
         */
        HIERARCHY,
    }
}
