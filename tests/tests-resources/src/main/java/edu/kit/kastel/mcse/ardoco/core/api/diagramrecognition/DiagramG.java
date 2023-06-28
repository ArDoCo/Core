package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;

public class DiagramG implements Diagram {
    private final File location;
    private final List<Box> properBoxes = new ArrayList<>();
    private final List<TextBox> properTextBoxes = new ArrayList<>();
    private final ImmutableSet<DiaTexTraceLink> traceLinks;

    @JsonCreator
    public DiagramG(@JsonProperty("name") String name, @JsonProperty("boxes") BoxG[] boxes) {
        location = new File("src/test/resources/Benchmark/teastore/diagrams_2018/Overview.jpg");
        addBoxes(boxes);
        traceLinks = Sets.immutable.fromStream(Arrays.stream(boxes).flatMap(b -> b.getTraceLinks().stream()));
    }

    private void addBoxes(BoxG[] boxes) {
        for (BoxG boxG : boxes) {
            addBox(boxG.toShallowBox());
            addBoxes(boxG.subBoxes);
        }
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
}
