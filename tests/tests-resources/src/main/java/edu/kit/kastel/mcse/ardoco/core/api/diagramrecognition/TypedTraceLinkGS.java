package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;

public record TypedTraceLinkGS(@JsonProperty("sentences") int[] sentences, @JsonProperty("traceType") TraceType traceType) implements Serializable {
}
