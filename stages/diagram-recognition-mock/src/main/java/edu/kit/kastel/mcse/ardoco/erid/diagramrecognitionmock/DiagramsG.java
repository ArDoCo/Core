package edu.kit.kastel.mcse.ardoco.erid.diagramrecognitionmock;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiagramsG {
    @JsonProperty("$schema")
    public String schema;
    public DiagramG[] diagrams;
}
