/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import java.util.Locale;
import java.util.Objects;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;

/**
 * @author Jan Keim
 */
public class MissingTextForModelElementInconsistency implements Inconsistency {
    private static final String INCONSISTENCY_TYPE_NAME = "MissingTextForModelElement";

    private static final String REASON_FORMAT_STRING = "Model contains an Instance that should be documented (because it is not whitelisted and its type \"%s\" is configured to need documentation) but could not be found in documentation: %s";

    private final ModelInstance instance;

    public MissingTextForModelElementInconsistency(ModelInstance instance) {
        this.instance = instance;
    }

    @Override
    public Inconsistency createCopy() {
        return new MissingTextForModelElementInconsistency(instance.createCopy());
    }

    @Override
    public String getReason() {
        return String.format(Locale.US, REASON_FORMAT_STRING, instance.getFullType(), instance.getFullName());
    }

    @Override
    public String getType() {
        return INCONSISTENCY_TYPE_NAME;
    }

    @Override
    public String toString() {
        return "MissingTextForModelElementInconsistency [modelInstance=" + instance + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (MissingTextForModelElementInconsistency) obj;
        return Objects.equals(instance, other.instance);
    }

    @Override
    public ImmutableCollection<String[]> toFileOutput() {
        String[] entry = { getType(), instance.getFullName(), instance.getFullType() };
        var list = Lists.mutable.<String[]> empty();
        list.add(entry);
        return list.toImmutable();
    }

}
