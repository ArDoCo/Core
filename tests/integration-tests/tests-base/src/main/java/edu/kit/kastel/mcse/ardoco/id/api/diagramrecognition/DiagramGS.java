/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.api.diagramrecognition;

import static edu.kit.kastel.mcse.ardoco.core.common.JsonHandling.createObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.collections.api.factory.Lists;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.*;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.DiagramGoldStandardTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.id.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.id.tests.eval.GoldStandardDiagrams;

/**
 * Implementation of the {@link Diagram} interface used for JSON deserialization. Instances are created from a
 * {@link GoldStandardDiagrams GoldStandardDiagrams}.
 */
@Deterministic
public class DiagramGS implements Diagram {
    private String resourceName;
    private List<Box> properBoxes = new ArrayList<>();
    private List<TextBox> properTextBoxes = new ArrayList<>();
    private DiagramProject project;

    /**
     * JSON constructor for deserialization
     *
     * @param project      the project this diagram belongs to
     * @param resourceName the resource name of this diagram
     * @param boxesNode    the unprocessed JSON node representing the top-level diagram boxes
     * @throws JsonProcessingException can occur during deserialization if the structure does not match
     */
    @JsonCreator
    public DiagramGS(@JacksonInject DiagramProject project, @JsonProperty("path") String resourceName, @JsonProperty("boxes") JsonNode boxesNode)
            throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BoundingBox.class, new BoundingBoxDeserializer());
        module.addDeserializer(TextBox.class, new TextBoxDeserializer());
        ObjectMapper objectMapper = createObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.setInjectableValues(new InjectableValues.Std().addValue(DiagramGS.class, this));
        var boxes = objectMapper.treeToValue(boxesNode, BoxGS[].class);
        this.project = project;

        if (!project.getDiagramResourceNames().contains(resourceName)) {
            var closest = project.getDiagramResourceNames()
                    .stream()
                    .max(Comparator.comparingDouble(a -> new WordSimUtils().getSimilarity(a, resourceName)))
                    .orElse("NONE");
            throw new IllegalArgumentException(String.format("The resource name \"%s\" doesn't match any known resource of \"%s\". Did you mean \"%s\"?",
                    resourceName, project.getProjectName(), closest));
        }

        this.resourceName = resourceName;
        addBoxes(boxes);
    }

    DiagramGS(DiagramProject project, String path, BoxGS[] boxes) {
        this.project = project;
        this.resourceName = path;
        addBoxes(boxes);
    }

    private void addBoxes(BoxGS[] boxes) {
        for (BoxGS boxGS : boxes) {
            addBox(boxGS);
            addBoxes(boxGS.getSubBoxes());
        }
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof DiagramGS other) {
            return resourceName.equals(other.resourceName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(resourceName);
    }

    @Override
    public File getLocation() {
        return project.getDiagramsGoldStandardFile();
    }

    @Override
    public void addBox(Box box) {
        properBoxes.add(box);
    }

    @Override
    public boolean removeBox(Box box) {
        return properBoxes.remove(box);
    }

    @Override
    public List<Box> getBoxes() {
        return List.copyOf(properBoxes);
    }

    @Override
    public void addTextBox(TextBox textBox) {
        properTextBoxes.add(textBox);
    }

    @Override
    public boolean removeTextBox(TextBox textBox) {
        return properTextBoxes.remove(textBox);
    }

    @Override
    public List<TextBox> getTextBoxes() {
        return List.copyOf(properTextBoxes);
    }

    @Override
    public void addConnector(Connector connector) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeConnector(Connector connector) {
        throw new NotImplementedException();
    }

    @Override
    public List<Connector> getConnectors() {
        throw new NotImplementedException();
    }

    /**
     * {@return the list of diagram-sentence trace links associated with this diagram} The sentences are used to resolve the sentence numbers to actual
     * sentences from the document.
     *
     * @param sentences the sentences from the text
     */
    public List<DiagramGoldStandardTraceLink> getTraceLinks(List<Sentence> sentences) {
        var traceLinks = Lists.mutable.<DiagramGoldStandardTraceLink>empty();
        for (Box box : properBoxes) {
            if (box instanceof BoxGS boxGS) {
                var boxTLs = boxGS.getTraceLinks(sentences).stream().toList();
                traceLinks.addAll(boxTLs);
            }
        }
        assert traceLinks.size() == traceLinks.distinct().size(); //Otherwise there are duplicates in the goldstandard
        return traceLinks;
    }

    /**
     * Retrieves a distinct list of diagram text trace links associated with the diagram
     *
     * @param textGoldstandard Partial path to the goldstandard text file
     * @return List of tracelinks
     */
    public List<DiagramGoldStandardTraceLink> getTraceLinks(List<Sentence> sentences, String textGoldstandard) {
        if (textGoldstandard == null)
            return getTraceLinks(sentences);
        return getTraceLinks(sentences).stream().filter(t -> textGoldstandard.contains(t.getGoldStandard())).distinct().toList();
    }

    public DiagramProject getDiagramProject() {
        return project;
    }

    @Override
    public String toString() {
        return getShortResourceName();
    }
}
