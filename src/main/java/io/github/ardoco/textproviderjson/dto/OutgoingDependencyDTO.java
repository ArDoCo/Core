package io.github.ardoco.textproviderjson.dto;

import com.fasterxml.jackson.annotation.*;
import io.github.ardoco.textproviderjson.DependencyType;

import java.util.Objects;

public class OutgoingDependencyDTO {
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
        OutgoingDependencyDTO that = (OutgoingDependencyDTO) o;
        return targetWordId == that.targetWordId && Objects.equals(dependencyType, that.dependencyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyType, targetWordId);
    }
}
