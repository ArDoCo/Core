/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("CodePackage")
public final class CodePackage extends CodeModule {

    private CodePackage() {
        // Jackson
    }

    public CodePackage(CodeItemRepository codeItemRepository, String name) {
        super(codeItemRepository, name, new TreeSet<>());
    }

    public CodePackage(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, name, content);
    }

    public SortedSet<CodePackage> getSubpackages() {
        SortedSet<CodePackage> codePackages = new TreeSet<>();
        for (CodeItem packageElement : getContent()) {
            if (packageElement instanceof CodePackage codePackage) {
                codePackages.add(codePackage);
            }
        }
        return codePackages;
    }

    public SortedSet<CodeCompilationUnit> getCompilationUnits() {
        SortedSet<CodeCompilationUnit> compilationUnits = new TreeSet<>();
        for (CodeItem packageElement : getContent()) {
            if (packageElement instanceof CodeCompilationUnit compilationUnit) {
                compilationUnits.add(compilationUnit);
            }
        }
        return compilationUnits;
    }

    @Override
    public SortedSet<? extends CodePackage> getAllPackages() {
        SortedSet<CodePackage> result = new TreeSet<>();
        result.add(this);
        getContent().forEach(c -> result.addAll(c.getAllPackages()));
        return result;
    }
}
