/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.models.java;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.ast.body.MethodDeclaration;

public class JavaMethod implements Serializable {

    private transient MethodDeclaration declaration;

    private String fullQualified;
    @JsonProperty
    private String name;
    @JsonProperty
    private String type;
    @JsonProperty
    private String javadoc;

    public JavaMethod() {
        // Jackson
    }

    public JavaMethod(MethodDeclaration declaration) {
        this.declaration = declaration;
        load();
    }

    private void load() {
        this.fullQualified = declaration.getDeclarationAsString(true, true);
        this.name = declaration.getNameAsString();
        this.type = declaration.getType().asString();
        var javadocComment = declaration.getJavadocComment();
        javadocComment.ifPresent(comment -> this.javadoc = comment.getContent());

    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    public String getJavadocContent() {
        return javadoc;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullQualified);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (JavaMethod) obj;
        return Objects.equals(fullQualified, other.fullQualified);
    }

    @Override
    public String toString() {
        return fullQualified;
    }

}
