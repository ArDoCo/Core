/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding;

/**
 * Describes a labeled edge.
 *
 * @param <L>
 *            The edge label type.
 */
public interface LabeledEdge<L> {
    /**
     * Gets the label of the edge.
     *
     * @return The label.
     */
    L getLabel();
}
