package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;

public class DiagramG implements Diagram {
    private final String path;
    private final List<Box> properBoxes = new ArrayList<>();
    private final List<TextBox> properTextBoxes = new ArrayList<>();
    private final ImmutableSet<DiaTexTraceLink> traceLinks;
    private final File location;

    @JsonCreator
    public DiagramG(@JacksonInject File location, @JsonProperty("path") String path, @JsonProperty("boxes") JsonNode boxesNode) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setInjectableValues(new InjectableValues.Std().addValue(DiagramG.class, this));
        var boxes = objectMapper.treeToValue(boxesNode, BoxG[].class);

        this.location = location;
        this.path = path;
        this.traceLinks = addBoxes(boxes);
    }

    private ImmutableSet<DiaTexTraceLink> addBoxes(BoxG[] boxes) {
        var traceLinks = Sets.mutable.<DiaTexTraceLink>empty();
        for (BoxG boxG : boxes) {
            addBox(boxG);
            addBoxes(boxG.getSubBoxes());
            traceLinks.addAll(boxG.getTraceLinks().toList());
            traceLinks.addAll(Arrays.stream(boxG.getSubBoxes()).flatMap(b -> b.getTraceLinks().stream()).toList());
        }
        return traceLinks.toImmutable();
    }

    public String getPath() {
        return path;
    }

    public String getShortPath() {
        var split = getPath().split("/|\\\\");
        return split[split.length - 1];
    }

    @Override
    public File getLocation() {
        return location;
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

    public ImmutableSet<DiaTexTraceLink> getTraceLinks() {
        return traceLinks;
    }

    /**
     * Retrieves a set of diagram text trace links associated with the diagram
     *
     * @param textGoldstandard Partial path to the goldstandard text file
     * @return Set of tracelinks
     */
    public @NotNull ImmutableSet<DiaTexTraceLink> getTraceLinks(@Nullable String textGoldstandard) {
        if (textGoldstandard == null)
            return getTraceLinks();
        return Sets.immutable.fromStream(traceLinks.stream().filter(t -> t.getGoldStandard().map(textGoldstandard::contains).orElse(false)));
    }

    @Override
    public String toString() {
        return getShortPath();
    }
}
