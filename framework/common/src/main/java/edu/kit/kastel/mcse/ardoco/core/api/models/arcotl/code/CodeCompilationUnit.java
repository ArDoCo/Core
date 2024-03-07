/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A compilation unit of a code model. A possible candidate for the code endpoint of a trace link that connects corresponding elements of an architecture model
 * and a code model.
 */
@JsonTypeName("CodeCompilationUnit")
public final class CodeCompilationUnit extends CodeModule {

    @JsonProperty
    private List<String> pathElements;
    @JsonProperty
    private String extension;
    @JsonProperty
    private ProgrammingLanguage language;

    @SuppressWarnings("unused")
    private CodeCompilationUnit() {
        // Jackson
    }

    public CodeCompilationUnit(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content, List<String> pathElements,
            String extension, ProgrammingLanguage language) {
        super(codeItemRepository, name, content);
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
    public SortedSet<CodeCompilationUnit> getAllCompilationUnits() {
        SortedSet<CodeCompilationUnit> result = new TreeSet<>();
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
        StringBuilder pathBuilder = new StringBuilder();
        for (String pathElement : pathElements) {
            pathBuilder.append(pathElement).append("/");
        }
        String ending = "";
        if (!extension.isEmpty()) {
            ending = "." + extension;
        }
        pathBuilder.append(getName()).append(ending);
        return pathBuilder.toString();
    }

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CodeCompilationUnit that))
            return false;
        if (!super.equals(o))
            return false;

        if (!Objects.equals(pathElements, that.pathElements))
            return false;
        if (!Objects.equals(extension, that.extension))
            return false;
        return language == that.language;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (pathElements != null ? pathElements.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        return result;
    }
}
