package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiagramsGS {
    @JsonProperty("$schema")
    public String schema;
    public DiagramGS[] diagrams;
}