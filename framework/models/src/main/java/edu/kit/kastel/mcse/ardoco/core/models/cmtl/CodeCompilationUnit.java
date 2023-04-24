package edu.kit.kastel.mcse.ardoco.core.models.cmtl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A compilation unit of a code model. A possible candidate for the code endpoint of a
 * trace link that connects corresponding elements of an architecture model and
 * a code model.
 */
public class CodeCompilationUnit extends CodeModule {

    private List<String> pathElements;
    private String extension;
    private ProgrammingLanguage language;

    public CodeCompilationUnit(String name, Set<? extends CodeItem> content, List<String> pathElements, String extension, ProgrammingLanguage language) {
        super(name, content);
        this.pathElements = new ArrayList<>(pathElements);
        this.extension = extension;
        this.language = language;
    }

    public ProgrammingLanguage getLanguage() {
        return language;
    }

    @Override
    public Set<Datatype> getAllDatatypes() {
        Set<Datatype> result = new HashSet<>();
        getContent().forEach(c -> result.addAll(c.getAllDatatypes()));
        return result;
    }

    @Override
    public Set<CodeCompilationUnit> getAllCompilationUnits() {
        Set<CodeCompilationUnit> result = new HashSet<>();
        result.add(this);
        return result;
    }

    public List<String> getParentPackageNames() {
        List<String> parents = new ArrayList<>();
        CodeModule parent = this;
        while (parent.hasParent()) {
            parent = parent.getParent();
            if (parent instanceof CodePackage) {
                parents.add(0, parent.getName());
            }
        }
        return parents;
    }

    public List<String> getPathElements() {
        return new ArrayList<>(pathElements);
    }

    public String getPath() {
        String path = "";
        for (String pathElement : pathElements) {
            path += (pathElement + "/");
        }
        String ending = "";
        if (!extension.isEmpty()) {
            ending = "." + extension;
        }
        return path + getName() + ending;
    }

    @Override
    public String toString() {
        return getPath();
    }
}
