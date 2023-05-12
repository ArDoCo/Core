/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A compilation unit of a code model. A possible candidate for the code endpoint of a
 * trace link that connects corresponding elements of an architecture model and
 * a code model.
 */
@JsonTypeName("CodeCompilationUnit")
public class CodeCompilationUnit extends CodeModule {

    @JsonProperty
    private List<String> pathElements;
    @JsonProperty
    private String extension;
    @JsonProperty
    private ProgrammingLanguage language;

    private CodeCompilationUnit() {
        // Jackson
    }

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
    public List<Datatype> getAllDataTypes() {
        List<Datatype> result = new ArrayList<>();
        getContent().forEach(c -> result.addAll(c.getAllDataTypes()));
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
