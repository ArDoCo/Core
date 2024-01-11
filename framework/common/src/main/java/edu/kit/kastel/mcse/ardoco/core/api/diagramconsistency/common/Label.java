/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

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
