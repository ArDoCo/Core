package io.github.ardoco.textproviderjson;

import com.fasterxml.jackson.annotation.*;

// TODO: Redo this. Converter did something odd
public class OutgoingDependency {
    private Object dependencyType;
    private long targetWordId;

    @JsonProperty("dependencyType")
    public Object getDependencyType() { return dependencyType; }
    @JsonProperty("dependencyType")
    public void setDependencyType(Object value) { this.dependencyType = value; }

    /**
     * The id of the word the dependency points to.
     */
    @JsonProperty("targetWordId")
    public long getTargetWordId() { return targetWordId; }
    @JsonProperty("targetWordId")
    public void setTargetWordId(long value) { this.targetWordId = value; }
}
