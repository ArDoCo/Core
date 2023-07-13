/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

/**
 * A code item of a code model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ //
        @JsonSubTypes.Type(value = CodeModule.class, name = "CodeModule"),//
        @JsonSubTypes.Type(value = ComputationalObject.class, name = "ComputationalObject"), //
        @JsonSubTypes.Type(value = Datatype.class, name = "Datatype") //
})
public abstract class CodeItem extends Entity {

    @JsonIgnore
    protected CodeItemRepository codeItemRepository;

    CodeItem() {
        // Jackson
    }

    /**
     * Creates a new code item with the specified name.
     *
     * @param name the name of the code item to be created
     */
    protected CodeItem(CodeItemRepository codeItemRepository, String name) {
        super(name);
        this.codeItemRepository = Objects.requireNonNull(codeItemRepository);
        this.codeItemRepository.addCodeItem(this);
    }

    void registerCurrentCodeItemRepository(CodeItemRepository codeItemRepository) {
        codeItemRepository.addCodeItem(this);
        this.codeItemRepository = codeItemRepository;
    }

    /**
     * Returns the content of this code item.
     *
     * @return the content of this code item
     */
    public List<CodeItem> getContent() {
        return new ArrayList<>();
    }

    public List<Datatype> getAllDataTypes() {
        return new ArrayList<>();
    }

    public Set<CodeItem> getAllDataTypesAndSelf() {
        Set<CodeItem> result = new HashSet<>(getAllDataTypes());
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
