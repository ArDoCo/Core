package io.github.ardoco.textproviderjson;

import com.fasterxml.jackson.annotation.*;

// TODO: Redo this. Converter did something odd
public class IncomingDependency {
    private DependencyType dependencyType;
    private long sourceWordId;

    @JsonProperty("dependencyType")
    public DependencyType getDependencyType() { return dependencyType; }
    @JsonProperty("dependencyType")
    public void setDependencyType(DependencyType value) { this.dependencyType = value; }

    /**
     * The id of the word the dependency originates from.
     */
    @JsonProperty("sourceWordId")
    public long getSourceWordId() { return sourceWordId; }
    @JsonProperty("sourceWordId")
    public void setSourceWordId(long value) { this.sourceWordId = value; }
}
