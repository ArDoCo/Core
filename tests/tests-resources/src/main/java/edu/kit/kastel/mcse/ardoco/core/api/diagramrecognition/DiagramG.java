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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;

public class DiagramG implements Diagram {
    private final String path;
    private final List<Box> properBoxes = new ArrayList<>();
    private final List<TextBox> properTextBoxes = new ArrayList<>();
    private final ImmutableSet<DiaTexTraceLink> traceLinks;

    @JsonCreator
    public DiagramG(@JsonProperty("path") String path, @JsonProperty("boxes") BoxG[] boxes) {
        this.path = path;
        addBoxes(boxes);
        this.traceLinks = Sets.immutable.fromStream(Arrays.stream(boxes).flatMap(b -> b.getTraceLinks().stream()));
    }

    private void addBoxes(BoxG[] boxes) {
        for (BoxG boxG : boxes) {
            addBox(boxG);
            addBoxes(boxG.subBoxes);
        }
    }

    public String getPath() {
        return path;
    }

    @Override
    public File getLocation() {
        throw new UnsupportedOperationException();
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
        return Sets.immutable.fromStream(traceLinks.stream().filter(t -> textGoldstandard.contains(t.getGoldStandard())));
    }
}
