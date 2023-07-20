package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;

public record TypedTraceLinkG(int[] sentences, TraceType traceType) implements Serializable {
}
