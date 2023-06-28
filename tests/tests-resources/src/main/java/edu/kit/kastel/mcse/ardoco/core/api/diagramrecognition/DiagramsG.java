package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiagramsG {
    @JsonProperty("$schema")
    public String schema;
    public DiagramG[] diagrams;
}
