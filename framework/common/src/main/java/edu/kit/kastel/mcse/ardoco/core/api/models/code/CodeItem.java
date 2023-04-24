/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.util.HashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

/**
 * A code item of a code model.
 */
public abstract class CodeItem extends Entity {

    /**
     * Creates a new code item with the specified name.
     *
     * @param name the name of the code item to be created
     */
    public CodeItem(String name) {
        super(name);
    }

    /**
     * Returns the content of this code item.
     *
     * @return the content of this code item
     */
    public Set<CodeItem> getContent() {
        return new HashSet<>();
    }

    public Set<Datatype> getAllDatatypes() {
        return new HashSet<>();
    }

    public Set<CodeItem> getAllDatatypesAndSelf() {
        Set<CodeItem> result = new HashSet<>(getAllDatatypes());
        result.add(this);
        return result;
    }

    public Set<ControlElement> getDeclaredMethods() {
        Set<ControlElement> methods = new HashSet<>();
        for (CodeItem codeItem : getContent()) {
            if (codeItem instanceof ControlElement codeMethod) {
                methods.add(codeMethod);
            }
        }
        return methods;
    }

    public Set<CodeCompilationUnit> getAllCompilationUnits() {
        return new HashSet<>();
    }

    public Set<? extends CodePackage> getAllPackages() {
        return new HashSet<>();
    }
}
