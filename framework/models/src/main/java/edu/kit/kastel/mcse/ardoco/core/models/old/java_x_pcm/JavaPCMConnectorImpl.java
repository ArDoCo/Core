/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.old.java_x_pcm;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.common.JavaUtils;
import edu.kit.kastel.mcse.ardoco.core.common.JsonUtils;
import edu.kit.kastel.mcse.ardoco.core.models.old.java.JavaClassOrInterface;
import edu.kit.kastel.mcse.ardoco.core.models.old.java.JavaProject;
import edu.kit.kastel.mcse.ardoco.core.models.old.pcm.PCMComponent;
import edu.kit.kastel.mcse.ardoco.core.models.old.pcm.PCMModel;

public class JavaPCMConnectorImpl implements JavaPCMConnector, Serializable {

    @Serial
    private static final long serialVersionUID = 8708747949561295415L;

    private static final Logger logger = LoggerFactory.getLogger(JavaPCMConnectorImpl.class);

    private final transient ObjectMapper oom = JsonUtils.createObjectMapper();
    private final transient PCMModel pcmModel;
    private final transient JavaProject javaModel;

    @JsonProperty
    private Map<String, String> javaClassToPCM = new HashMap<>();
    @JsonProperty
    private Map<String, String> javaInterfaceToPCM = new HashMap<>();

    public JavaPCMConnectorImpl(JavaProject javaModel, PCMModel pcmModel) {
        this.javaModel = Objects.requireNonNull(javaModel);
        this.pcmModel = Objects.requireNonNull(pcmModel);
    }

    public void loadMappingFromJson(InputStream is) {
        try {
            var data = oom.readValue(is, JavaPCMConnectorImpl.class);
            this.javaClassToPCM = new HashMap<>(data.javaClassToPCM);
            this.javaInterfaceToPCM = new HashMap<>(data.javaInterfaceToPCM);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String saveToString() {
        try {
            return oom.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public void addClassToComponent(JavaClassOrInterface javaClass, PCMComponent component) {
        ensureComponent(component);
        ensureClass(javaClass);
        String javaClassId = classId(javaClass);
        if (javaClassToPCM.containsKey(javaClassId)) {
            throw new IllegalStateException("Java Class " + javaClass + " is already contained in a component!");
        }
        javaClassToPCM.put(javaClassId, component.getId());
    }

    public void addInterfaceToComponent(JavaClassOrInterface javaInterface, PCMComponent component) {
        ensureComponent(component);
        ensureInterface(javaInterface);
        String javaInterfaceId = classId(javaInterface);
        if (javaInterfaceToPCM.containsKey(javaInterfaceId)) {
            throw new IllegalStateException("Java Interface " + javaInterface + " is already contained in a component!");
        }
        javaInterfaceToPCM.put(javaInterfaceId, component.getId());
    }

    @Override
    public Set<JavaClassOrInterface> getClassesThatBelongToComponent(PCMComponent component) {
        return JavaUtils.reverseMap(this.javaClassToPCM)
                        .getOrDefault(component.getId(), Set.of())
                        .stream()
                        .map(id -> classById(id, true))
                        .collect(Collectors.toSet());
    }

    @Override
    public Set<JavaClassOrInterface> getInterfacesThatBelongToComponent(PCMComponent component) {
        return JavaUtils.reverseMap(this.javaInterfaceToPCM)
                        .getOrDefault(component.getId(), Set.of())
                        .stream()
                        .map(id -> classById(id, false))
                        .collect(Collectors.toSet());
    }

    private JavaClassOrInterface classById(String id, boolean isClass) {
        return javaModel.getClassesAndInterfaces()
                        .stream()
                        .filter(c -> Objects.equals(id, classId(c)) && c.isInterface() != isClass)
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("Cannot find element with id=" + id + " and isClass=" + isClass));
    }

    @Override
    public PCMComponent getComponentOfClassOrInterface(JavaClassOrInterface classOrInterface) {
        String id = classId(classOrInterface);
        if (javaInterfaceToPCM.containsKey(id))
            return componentById(javaInterfaceToPCM.get(id));
        if (javaClassToPCM.containsKey(id))
            return componentById(javaClassToPCM.get(id));
        return null;
    }

    private PCMComponent componentById(String id) {
        return this.pcmModel.getRepository().getComponents().stream().filter(c -> Objects.equals(c.getId(), id)).findFirst().orElseThrow();
    }

    private void ensureComponent(PCMComponent component) {
        if (component == null || pcmModel.getRepository().getComponents().stream().noneMatch(c -> Objects.equals(c.getId(), component.getId())))
            throw new IllegalArgumentException("Could not find component with id " + (component == null ? null : component.getId()));
    }

    private void ensureClass(JavaClassOrInterface javaClass) {
        if (javaClass == null || javaClass.isInterface())
            throw new IllegalArgumentException("JavaClass " + javaClass + " is not a class");
        if (!javaModel.getClasses().contains(javaClass))
            throw new IllegalArgumentException("Java Class is not part of the Java Model");
    }

    private void ensureInterface(JavaClassOrInterface javaInterface) {
        if (javaInterface == null || !javaInterface.isInterface())
            throw new IllegalArgumentException("JavaClass " + javaInterface + " is not a class");
        if (!javaModel.getClassesAndInterfaces().contains(javaInterface))
            throw new IllegalArgumentException("Java Class is not part of the Java Model");
    }

    private String classId(JavaClassOrInterface javaClass) {
        return javaClass.getFullyQualifiedName();
    }

}
