/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage2;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.ElementRole;

/**
 * Links a box (diagram element) and a model element.
 *
 * @param boxId
 *                       The id of the box.
 * @param modelElementId
 *                       The id of the model element.
 * @param role
 *                       The role of the box.
 */
public record Link(@JsonProperty("diagram_id") int boxId, @JsonProperty("model_id") String modelElementId, @JsonProperty("role") ElementRole role) {
}
