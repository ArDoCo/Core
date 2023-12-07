/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
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
public sealed class Datatype extends CodeItem permits ClassUnit, InterfaceUnit {

    @JsonProperty
    private String compilationUnitId;
    @JsonProperty
    private List<String> extendedDataTypesIds;
    @JsonProperty
    private List<String> implementedDataTypesIds;

    Datatype() {
        // Jackson
    }

    public Datatype(CodeItemRepository codeItemRepository, String name) {
        super(codeItemRepository, name);
        this.extendedDataTypesIds = new ArrayList<>();
        this.implementedDataTypesIds = new ArrayList<>();
    }

    public CodeCompilationUnit getCompilationUnit() {
        CodeItem codeItem = codeItemRepository.getCodeItem(compilationUnitId);
        if (codeItem instanceof CodeCompilationUnit codeCompilationUnit) {
            return codeCompilationUnit;
        }
        return null;
    }

    public SortedSet<Datatype> getExtendedTypes() {
        return extendedDataTypesIds.stream().map(id -> {
            CodeItem codeItem = codeItemRepository.getCodeItem(id);
            if (codeItem instanceof Datatype datatype) {
                return datatype;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
    }

    public SortedSet<Datatype> getImplementedTypes() {
        return implementedDataTypesIds.stream().map(id -> {
            CodeItem codeItem = codeItemRepository.getCodeItem(id);
            if (codeItem instanceof Datatype datatype) {
                return datatype;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
    }

    public void setCompilationUnit(CodeCompilationUnit compilationUnit) {
        this.compilationUnitId = compilationUnit.getId();
    }

    public void setExtendedTypes(SortedSet<Datatype> extendedDatatypes) {
        for (Datatype datatype : extendedDatatypes) {
            this.extendedDataTypesIds.add(datatype.getId());
        }
    }

    public void setImplementedTypes(SortedSet<Datatype> implementedDatatypes) {
        for (Datatype datatype : implementedDatatypes) {
            this.implementedDataTypesIds.add(datatype.getId());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Datatype datatype))
            return false;
        if (!super.equals(o))
            return false;

        if (!Objects.equals(compilationUnitId, datatype.compilationUnitId))
            return false;
        if (!Objects.equals(extendedDataTypesIds, datatype.extendedDataTypesIds))
            return false;
        return Objects.equals(implementedDataTypesIds, datatype.implementedDataTypesIds);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (compilationUnitId != null ? compilationUnitId.hashCode() : 0);
        result = 31 * result + (extendedDataTypesIds != null ? extendedDataTypesIds.hashCode() : 0);
        result = 31 * result + (implementedDataTypesIds != null ? implementedDataTypesIds.hashCode() : 0);
        return result;
    }
}
