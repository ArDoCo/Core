/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * All supported types of inconsistencies.
 */
public enum InconsistencyType {
    /**
     * The name of the model element is inconsistent between the architecture and the code.
     */
    @JsonProperty("name_inconsistency") NAME_INCONSISTENCY,
    /**
     * The hierarchy of the model element is inconsistent between the architecture and the code.
     */
    @JsonProperty("hierarchy_inconsistency") HIERARCHY_INCONSISTENCY,
    /**
     * A line was expected in the diagram but is missing.
     */
    @JsonProperty("missing_line") MISSING_LINE,
    /**
     * A box was expected in the diagram but is missing.
     */
    @JsonProperty("missing_box") MISSING_BOX,
    /**
     * A line was found in the diagram but was not expected.
     */
    @JsonProperty("unexpected_line") UNEXPECTED_LINE,
    /**
     * A box was found in the diagram but was not expected.
     */
    @JsonProperty("unexpected_box") UNEXPECTED_BOX,
}
