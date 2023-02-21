package io.github.ardoco.textproviderjson;

import com.fasterxml.jackson.annotation.*;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncomingDependency that = (IncomingDependency) o;
        return sourceWordId == that.sourceWordId && dependencyType == that.dependencyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyType, sourceWordId);
    }
}
