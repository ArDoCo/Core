/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("CodePackage")
public class CodePackage extends CodeModule {

    private CodePackage() {
        // Jackson
    }

    public CodePackage(CodeItemRepository codeItemRepository, String name) {
        super(codeItemRepository, name, new HashSet<>());
    }

    public CodePackage(CodeItemRepository codeItemRepository, String name, Set<? extends CodeItem> content) {
        super(codeItemRepository, name, content);
    }

    public Set<CodePackage> getSubpackages() {
        Set<CodePackage> codePackages = new HashSet<>();
        for (CodeItem packageElement : getContent()) {
            if (packageElement instanceof CodePackage codePackage) {
                codePackages.add(codePackage);
            }
        }
        return codePackages;
    }

    public Set<CodeCompilationUnit> getCompilationUnits() {
        Set<CodeCompilationUnit> compilationUnits = new HashSet<>();
        for (CodeItem packageElement : getContent()) {
            if (packageElement instanceof CodeCompilationUnit compilationUnit) {
                compilationUnits.add(compilationUnit);
            }
        }
        return compilationUnits;
    }

    @Override
    public Set<? extends CodePackage> getAllPackages() {
        Set<CodePackage> result = new HashSet<>();
        result.add(this);
        getContent().forEach(c -> result.addAll(c.getAllPackages()));
        return result;
    }
}
