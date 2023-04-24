package edu.kit.kastel.mcse.ardoco.core.models.cmtl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CodeModule extends CodeItem {

    private Optional<CodeModule> parent;
    private Set<CodeItem> content;

    public CodeModule(String name, Set<? extends CodeItem> content) {
        super(name);
        this.content = new HashSet<>(content);
        parent = Optional.empty();
    }

    @Override
    public Set<CodeItem> getContent() {
        return new HashSet<>(content);
    }

    public void setContent(Set<? extends CodeItem> content) {
        this.content = new HashSet<>(content);
    }

    public void addContent(CodeItem content) {
        this.content.add(content);
    }

    public void addContent(Set<? extends CodeItem> content) {
        this.content.addAll(content);
    }

    public CodeModule getParent() {
        return parent.get();
    }

    public boolean hasParent() {
        return parent.isPresent();
    }

    public void setParent(CodeModule parent) {
        this.parent = Optional.of(parent);
    }

    @Override
    public Set<CodeCompilationUnit> getAllCompilationUnits() {
        Set<CodeCompilationUnit> result = new HashSet<>();
        getContent().forEach(c -> result.addAll(c.getAllCompilationUnits()));
        return result;
    }

    @Override
    public Set<? extends CodePackage> getAllPackages() {
        Set<CodePackage> result = new HashSet<>();
        getContent().forEach(c -> result.addAll(c.getAllPackages()));
        return result;
    }
}
