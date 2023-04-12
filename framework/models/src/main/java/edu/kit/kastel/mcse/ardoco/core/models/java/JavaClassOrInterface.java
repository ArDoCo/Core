/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public class JavaClassOrInterface implements Serializable {
    private transient ClassOrInterfaceDeclaration declaration;

    @JsonProperty
    private String name;
    @JsonProperty
    private String fullyQualifiedName;
    @JsonProperty
    private boolean isInterface;
    @JsonProperty
    private List<CodeComment> codeComments;
    @JsonProperty
    private List<JavaMethod> allMethods;
    @JsonProperty
    private List<Constructor> allConstructors;

    public JavaClassOrInterface() {
        // Jackson
    }

    public JavaClassOrInterface(ClassOrInterfaceDeclaration declaration) {
        this.declaration = declaration;
        load();
    }

    private void load() {
        this.name = this.declaration.getNameAsString();
        this.fullyQualifiedName = this.declaration.getFullyQualifiedName().orElse(this.name);
        this.isInterface = declaration.isInterface();
        this.codeComments = declaration.getAllContainedComments()
                .stream()
                .map(c -> new CodeComment(c.getClass().getSimpleName(), c.getContent(), c.getRange().map(r -> r.begin.line).orElse(-1), c.getCommentedNode()
                        .isEmpty()))
                .toList();
        this.allMethods = declaration.getMethods().stream().map(JavaMethod::new).toList();
        this.allConstructors = declaration.getConstructors().stream().map(Constructor::new).toList();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the fullyQualifiedName
     */
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    /**
     * @return true, iff this is an interface
     */
    public boolean isInterface() {
        return isInterface;
    }

    public List<CodeComment> getAllComments() {
        return new ArrayList<>(codeComments);
    }

    public List<JavaMethod> getAllMethods() {
        return new ArrayList<>(allMethods);
    }

    public List<Constructor> getConstructors() {
        return new ArrayList<>(allConstructors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (JavaClassOrInterface) obj;
        return Objects.equals(fullyQualifiedName, other.fullyQualifiedName);
    }

    @Override
    public String toString() {
        return "JavaClassOrInterface [" + (getFullyQualifiedName() != null ?
                "getFullyQualifiedName()=" + getFullyQualifiedName() + ", " :
                "") + "isInterface()=" + isInterface() + "]";
    }

}
