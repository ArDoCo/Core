/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.informalin.framework.models.java.JavaClassOrInterface;
import edu.kit.kastel.informalin.framework.models.java.JavaProject;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;

public class JavaJsonModelConnector implements ModelConnector {
    private final JavaProject javaProject;

    public JavaJsonModelConnector(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public JavaJsonModelConnector(InputStream is) throws IOException {
        ObjectMapper oom = new ObjectMapper();
        oom.setVisibility(oom.getSerializationConfig()
                .getDefaultVisibilityChecker() //
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)//
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)//
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)//
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        this.javaProject = Objects.requireNonNull(oom.readValue(is, JavaProject.class));
    }

    @Override
    public String getModelId() {
        return this.javaProject.getId();
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.CODE;
    }

    @Override
    public ImmutableList<ModelInstance> getInstances() {
        return Lists.immutable.withAll(javaProject.getClassesAndInterfaces()).collect(this::toInstance);
    }

    private ModelInstanceImpl toInstance(JavaClassOrInterface javaClassOrInterface) {
        var name = javaClassOrInterface.getName();
        var identifier = javaClassOrInterface.getFullyQualifiedName();
        var type = javaClassOrInterface.isInterface() ? "Interface" : "Class";
        return new ModelInstanceImpl(name, type, identifier);
    }
}
