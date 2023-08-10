package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.collections.api.factory.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class DiagramGS implements Diagram, Comparable<DiagramGS> {
    private String resourceName;
    private List<Box> properBoxes = new ArrayList<>();
    private List<TextBox> properTextBoxes = new ArrayList<>();
    private DiagramProject project;

    @JsonCreator
    public DiagramGS(@JacksonInject DiagramProject project, @JsonProperty("path") String resourceName, @JsonProperty("boxes") JsonNode boxesNode)
            throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BoundingBox.class, new BoundingBoxDeserializer());
        module.addDeserializer(TextBox.class, new TextBoxDeserializer());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.setInjectableValues(new InjectableValues.Std().addValue(DiagramGS.class, this));
        var boxes = objectMapper.treeToValue(boxesNode, BoxGS[].class);

        this.project = project;
        this.resourceName = resourceName;
        addBoxes(boxes);
    }

    public DiagramGS(DiagramProject project, String path, BoxGS[] boxes) {
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
    public int compareTo(@NotNull DiagramGS o) {
        if (equals(o))
            return 0;
        return resourceName.compareTo(o.resourceName);
    }

    public String getShortPath() {
        var split = getResourceName().split("/|\\\\");
        return split[split.length - 1];
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

    public List<DiaGSTraceLink> getTraceLinks(List<Sentence> sentences) {
        var traceLinks = Lists.mutable.<DiaGSTraceLink>empty();
        for (Box box : properBoxes) {
            if (box instanceof BoxGS boxGS) {
                var boxTLs = boxGS.getTraceLinks(sentences).toList();
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
    public @NotNull List<DiaGSTraceLink> getTraceLinks(List<Sentence> sentences, @Nullable String textGoldstandard) {
        if (textGoldstandard == null)
            return getTraceLinks(sentences);
        return getTraceLinks(sentences).stream().filter(t -> textGoldstandard.contains(t.getGoldStandard())).distinct().toList();
    }

    public @NotNull DiagramProject getDiagramProject() {
        return project;
    }

    @Override
    public String toString() {
        return getShortPath();
    }
}
