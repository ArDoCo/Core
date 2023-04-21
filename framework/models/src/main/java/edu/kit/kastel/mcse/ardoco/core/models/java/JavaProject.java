/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JavaProject implements Serializable {
    @JsonProperty
    private String id;
    @JsonProperty
    private List<JavaClassOrInterface> classesAndInterfaces;

    public JavaProject() {
        classesAndInterfaces = new ArrayList<>();
    }

    /**
     * @param classOrInterface a java class or interface
     * @return add a {@link JavaClassOrInterface} to the project.
     */
    public boolean addClassOrInterface(JavaClassOrInterface classOrInterface) {
        return this.classesAndInterfaces.add(classOrInterface);
    }

    /**
     * @return the classesAndInterfaces
     */
    public List<JavaClassOrInterface> getClassesAndInterfaces() {
        return new ArrayList<>(classesAndInterfaces);
    }

    /**
     * @return the classNames
     */
    public List<String> getClassNames() {
        return getClasses().stream().map(JavaClassOrInterface::getFullyQualifiedName).toList();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * @return the classes.
     */
    public List<JavaClassOrInterface> getClasses() {
        return getClassesAndInterfaces().stream().filter(Predicate.not(JavaClassOrInterface::isInterface)).toList();
    }

}
