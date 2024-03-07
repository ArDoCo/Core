/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.api.diagramrecognition;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates multiple {@link DiagramGS} instances from the gold standard.
 */
public class DiagramsGS {
    @JsonProperty("$schema")
    public String schema;
    public DiagramGS[] diagrams;
}
