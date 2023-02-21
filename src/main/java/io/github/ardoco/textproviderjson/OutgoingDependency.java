package io.github.ardoco.textproviderjson;

import com.fasterxml.jackson.annotation.*;

import java.util.Objects;

// TODO: Redo this. Converter did something odd
public class OutgoingDependency {
    private DependencyType dependencyType;
    private long targetWordId;

    @JsonProperty("dependencyType")
    public Object getDependencyType() { return dependencyType; }
    @JsonProperty("dependencyType")
    public void setDependencyType(DependencyType value) { this.dependencyType = value; }

    /**
     * The id of the word the dependency points to.
     */
    @JsonProperty("targetWordId")
    public long getTargetWordId() { return targetWordId; }
    @JsonProperty("targetWordId")
    public void setTargetWordId(long value) { this.targetWordId = value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutgoingDependency that = (OutgoingDependency) o;
        return targetWordId == that.targetWordId && Objects.equals(dependencyType, that.dependencyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyType, targetWordId);
    }
}
