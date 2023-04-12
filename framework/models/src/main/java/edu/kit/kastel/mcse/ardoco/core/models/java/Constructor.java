/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.java;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.ast.body.ConstructorDeclaration;

public class Constructor implements Serializable {

    private String identifier;
    @JsonProperty
    private String javadocComment;

    public Constructor() {
        // Jackson
    }

    public Constructor(ConstructorDeclaration declaration) {
        this.identifier = declaration.getDeclarationAsString(true, true);
        load(declaration);
    }

    private void load(ConstructorDeclaration declaration) {
        var javadocCommentJava = declaration.getJavadocComment();
        javadocCommentJava.ifPresent(comment -> this.javadocComment = comment.getContent());

    }

    public String getIdentifier() {
        return identifier;
    }

    public String getJavadocContent() {
        return javadocComment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(javadocComment, identifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (Constructor) obj;
        return Objects.equals(javadocComment, other.javadocComment) && Objects.equals(identifier, other.identifier);
    }

    @Override
    public String toString() {
        return identifier;
    }

}
