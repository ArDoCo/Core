package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

import java.util.List;
import java.util.Objects;

public class HoldBackModel extends Model {

    private final List<? extends Entity> content;
    private final List<? extends Entity> endpoints;

    public HoldBackModel(List<? extends Entity> content, List<? extends Entity> endpoints) {
        this.content = content;
        this.endpoints = endpoints;
    }

    @Override
    public List<? extends Entity> getContent() {
        return this.content;
    }

    @Override
    public List<? extends Entity> getEndpoints() {
        return this.endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        HoldBackModel that = (HoldBackModel) o;
        return Objects.equals(content, that.content) && Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), content, endpoints);
    }
}
