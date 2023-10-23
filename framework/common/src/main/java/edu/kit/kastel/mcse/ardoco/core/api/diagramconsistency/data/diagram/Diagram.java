package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.data.diagram;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import org.eclipse.jgit.annotations.Nullable;

/**
 * Represents a simple "boxes-and-lines" diagram, consisting of vertices (boxes) and edges (lines).
 * The JSON format of these diagrams consists of a list of 'boxes', where each box has a unique id and a text.
 * Additionally, each box can have a list of ids in 'lines_to' and 'contains'.
 * These specify to which nodes the outgoing lines go and which boxes are contained in this box respectively.
 */
@Deterministic public final class Diagram {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("source")
    private final String source;

    @JsonIgnore
    private final SortedMap<Integer, Box> boxes;

    @JsonIgnore
    private int nextId = 0;

    @JsonCreator
    private Diagram(@JsonProperty("name") String name, @JsonProperty("source") String source,
            @JsonProperty("boxes") @Nullable ArrayList<InternalBox> boxes) {
        this.name = name;
        this.source = source;
        this.boxes = new TreeMap<>();

        if (boxes == null) {
            return;
        }

        int maxId = 0;

        for (var box : boxes) {
            Box b = new Box(box.id, box.text);

            if (this.boxes.containsKey(box.id)) {
                throw new IllegalArgumentException("Duplicate box id: " + box.id);
            }

            this.boxes.put(box.id, b);
            maxId = Math.max(maxId, box.id);
        }

        this.nextId = maxId + 1;

        for (var box : boxes) {
            Box current = this.boxes.get(box.id);

            for (var targetId : box.lines) {
                Box target = this.boxes.get(targetId);
                current.addLineTo(target);
            }

            for (var containedId : box.contains) {
                Box contained = this.boxes.get(containedId);
                current.addContainedBox(contained);
            }
        }
    }

    /**
     * Creates a new diagram.
     *
     * @param name
     *         The name of the diagram.
     * @param source
     *         The source of the diagram. Could be an URL, file path or similar.
     */
    public Diagram(String name, String source) {
        this.name = name;
        this.source = source;
        this.boxes = new TreeMap<>();
    }

    /**
     * Adds a new box to the diagram.
     *
     * @param text
     *         The text of the box.
     * @return The box.
     */
    public Box addBox(String text) {
        Box box = new Box(this.nextId++, text);
        this.boxes.put(box.getId(), box);
        return box;
    }

    /**
     * Gets the name of the diagram.
     *
     * @return The name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the source of the diagram.
     *
     * @return The source. Could be an URL, file path or similar.
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Gets the boxes of the diagram.
     *
     * @return The boxes.
     */
    public Collection<Box> getBoxes() {
        return Collections.unmodifiableCollection(this.boxes.values());
    }

    /**
     * Gets a box by its id.
     *
     * @param id
     *         The id of the box.
     * @return The box.
     */
    public Box getBox(int id) {
        return this.boxes.get(id);
    }

    @JsonProperty("boxes")
    private List<InternalBox> getInternalBoxes() {
        List<InternalBox> internalBoxes = new ArrayList<>(this.boxes.size());

        for (var box : this.boxes.values()) {
            internalBoxes.add(new InternalBox(box.getText(), box.getId(), box.getOutgoingLines()
                    .stream()
                    .map(line -> line.target()
                            .getId())
                    .collect(Collectors.toCollection(ArrayList::new)), box.getContainedBoxes()
                    .stream()
                    .map(Box::getId)
                    .collect(Collectors.toCollection(ArrayList::new))));
        }

        return internalBoxes;
    }

    private static final class InternalBox {
        @JsonProperty("text")
        private final String text;
        @JsonProperty("id")
        private final int id;
        @JsonProperty("lines_to")
        private final List<Integer> lines;
        @JsonProperty("contains")
        private final List<Integer> contains;

        @JsonCreator
        private InternalBox(@JsonProperty("text") @Nullable String text, @JsonProperty("id") int id,
                @JsonProperty("lines_to") @Nullable ArrayList<Integer> outgoingLines,
                @JsonProperty("contains") @Nullable ArrayList<Integer> containedBoxes) {
            this.text = Objects.requireNonNullElse(text, "");
            this.id = id;
            this.lines = Objects.requireNonNullElse(outgoingLines, new ArrayList<>());
            this.contains = Objects.requireNonNullElse(containedBoxes, new ArrayList<>());
        }
    }
}
