/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage1;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.ElementRole;

/**
 * An occurrence of a diagram element in a model.
 *
 * @param role
 *                       The role of the element in the model.
 * @param modelElementId
 *                       The ID of the element in the model.
 */
public record Occurrence(@JsonProperty("role") ElementRole role, @JsonProperty("id") String modelElementId) {
}
