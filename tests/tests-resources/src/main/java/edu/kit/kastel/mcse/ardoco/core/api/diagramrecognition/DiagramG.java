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

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class DiagramG implements Diagram, Comparable<DiagramG> {
    private String path;
    private List<Box> properBoxes = new ArrayList<>();
    private List<TextBox> properTextBoxes = new ArrayList<>();
    private DiagramProject project;

    @JsonCreator
    public DiagramG(@JacksonInject DiagramProject project, @JsonProperty("path") String path, @JsonProperty("boxes") JsonNode boxesNode)
            throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setInjectableValues(new InjectableValues.Std().addValue(DiagramG.class, this));
        var boxes = objectMapper.treeToValue(boxesNode, BoxG[].class);

        this.project = project;
        this.path = path;
        addBoxes(boxes);
    }

    public DiagramG(DiagramProject project, String path, BoxG[] boxes) {
        this.project = project;
        this.path = path;
        addBoxes(boxes);
    }

    private void addBoxes(BoxG[] boxes) {
        for (BoxG boxG : boxes) {
            addBox(boxG);
            addBoxes(boxG.getSubBoxes());
        }
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof DiagramG other) {
            return path.equals(other.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }

    @Override
    public int compareTo(@NotNull DiagramG o) {
        if (equals(o))
            return 0;
        return path.compareTo(o.path);
    }

    public String getShortPath() {
        var split = getPath().split("/|\\\\");
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

    public List<DiaGSTraceLink> getTraceLinks() {
        var traceLinks = Lists.mutable.<DiaGSTraceLink>empty();
        for (Box box : properBoxes) {
            if (box instanceof BoxG boxG) {
                var boxTLs = boxG.getTraceLinks().toList();
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
    public @NotNull List<DiaGSTraceLink> getTraceLinks(@Nullable String textGoldstandard) {
        if (textGoldstandard == null)
            return getTraceLinks();
        return getTraceLinks().stream().filter(t -> textGoldstandard.contains(t.getGoldStandard())).distinct().toList();
    }

    public @NotNull DiagramProject getProject() {
        return project;
    }

    @Override
    public String toString() {
        return getShortPath();
    }
}
