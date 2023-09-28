/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import java.util.Locale;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.ModelInconsistency;

public class MissingTextForModelElementInconsistency implements ModelInconsistency {
    private static final String INCONSISTENCY_TYPE_NAME = "MissingTextForModelElement";

    private final Entity entity;

    public MissingTextForModelElementInconsistency(Entity entity) {
        this.entity = entity;
    }

    @Override
    public String getReason() {
        return String.format(Locale.US, "Model contains an Instance \"%s\" (type: \"%s\")  that seems to be undocumented.", entity.getName(), entity.getClass().getSimpleName());
    }

    @Override
    public String getType() {
        return INCONSISTENCY_TYPE_NAME;
    }

    @Override
    public String toString() {
        return "MissingTextForModelElementInconsistency [modelInstance=" + entity + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MissingTextForModelElementInconsistency other)) {
            return false;
        }
        return Objects.equals(entity, other.entity);
    }

    @Override
    public ImmutableCollection<String[]> toFileOutput() {
        String[] entry = { getType(), entity.getName(), entity.getClass().getSimpleName() };
        var list = Lists.mutable.<String[]>empty();
        list.add(entry);
        return list.toImmutable();
    }

    @Override
    public String getEntityName() {
        return entity.getName();
    }

    @Override
    public String getEntityType() {
        return entity.getClass().getSimpleName();
    }

    @Override
    public String getInstanceId() {
        return entity.getId();
    }
}
