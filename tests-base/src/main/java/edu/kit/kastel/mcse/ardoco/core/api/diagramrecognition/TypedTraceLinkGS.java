/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.TraceType;

/**
 * Encapsulates a diagram-sentence trace link of a specific type.
 * 
 * @param sentences
 * @param traceType
 */
public record TypedTraceLinkGS(@JsonProperty("sentences") int[] sentences, @JsonProperty("traceType") TraceType traceType) implements Serializable {
}
