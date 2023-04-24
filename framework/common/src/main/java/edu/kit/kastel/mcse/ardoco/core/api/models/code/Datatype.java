/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.util.HashSet;
import java.util.Set;

public class Datatype extends CodeItem {

    private CodeCompilationUnit compilationUnit;
    private Set<Datatype> extendedDatatypes;
    private Set<Datatype> implementedDatatypes;

    public Datatype(String name) {
        super(name);
        this.extendedDatatypes = new HashSet<>();
        this.implementedDatatypes = new HashSet<>();
    }

    public CodeCompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public Set<Datatype> getExtendedTypes() {
        return extendedDatatypes;
    }

    public Set<Datatype> getImplementedTypes() {
        return implementedDatatypes;
    }

    public void setCompilationUnit(CodeCompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    public void setExtendedTypes(Set<Datatype> extendedDatatypes) {
        this.extendedDatatypes = new HashSet<>(extendedDatatypes);
    }

    public void setImplementedTypes(Set<Datatype> implementedDatatypes) {
        this.implementedDatatypes = new HashSet<>(implementedDatatypes);
    }
}
