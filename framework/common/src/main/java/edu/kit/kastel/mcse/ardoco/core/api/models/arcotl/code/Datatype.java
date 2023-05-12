/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ //
        @JsonSubTypes.Type(value = ClassUnit.class, name = "ClassUnit"),//
        @JsonSubTypes.Type(value = InterfaceUnit.class, name = "InterfaceUnit") //
})
@JsonTypeName("Datatype")
public class Datatype extends CodeItem {

    @JsonProperty
    private String compilationUnitId;
    @JsonProperty
    private List<String> extendedDataTypesIds;
    @JsonProperty
    private List<String> implementedDataTypesIds;

    Datatype() {
        // Jackson
    }

    public Datatype(String name) {
        super(name);
        this.extendedDataTypesIds = new ArrayList<>();
        this.implementedDataTypesIds = new ArrayList<>();
    }

    public CodeCompilationUnit getCompilationUnit() {
        CodeItem codeItem = CodeItemRepository.getInstance().getCodeItem(compilationUnitId);
        if (codeItem instanceof CodeCompilationUnit codeCompilationUnit) {
            return codeCompilationUnit;
        }
        return null;
    }

    public Set<Datatype> getExtendedTypes() {
        return extendedDataTypesIds.stream().map(id -> {
            CodeItem codeItem = CodeItemRepository.getInstance().getCodeItem(id);
            if (codeItem instanceof Datatype datatype) {
                return datatype;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public Set<Datatype> getImplementedTypes() {
        return implementedDataTypesIds.stream().map(id -> {
            CodeItem codeItem = CodeItemRepository.getInstance().getCodeItem(id);
            if (codeItem instanceof Datatype datatype) {
                return datatype;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public void setCompilationUnit(CodeCompilationUnit compilationUnit) {
        this.compilationUnitId = compilationUnit.getId();
    }

    public void setExtendedTypes(Set<Datatype> extendedDatatypes) {
        for (Datatype datatype : extendedDatatypes) {
            this.extendedDataTypesIds.add(datatype.getId());
        }
    }

    public void setImplementedTypes(Set<Datatype> implementedDatatypes) {
        for (Datatype datatype : implementedDatatypes) {
            this.implementedDataTypesIds.add(datatype.getId());
        }
    }
}
