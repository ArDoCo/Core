package edu.kit.kastel.mcse.ardoco.core.models.cmtl;

import java.util.HashSet;
import java.util.Set;

public class CodePackage extends CodeModule {

    public CodePackage(String name) {
        super(name, new HashSet<>());
    }

    public CodePackage(String name, Set<? extends CodeItem> content) {
        super(name, content);
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
