package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;

public class DiaTexTraceLink implements Comparable<DiaTexTraceLink>, Serializable {
    protected final DiagramElement diagramElement;
    private final int sentenceNo;

    protected Text text;
    protected Set<DiaTexTraceLink> related = new LinkedHashSet<>();

    /**
     * Creates a tracelink between a diagram element and a sentence number
     *
     * @param diagramElement diagram element
     * @param sentenceNo     sentence number, indexing starts at 1
     */
    public DiaTexTraceLink(@NotNull DiagramElement diagramElement, int sentenceNo) {
        this.diagramElement = diagramElement;
        this.sentenceNo = sentenceNo;
    }

    public @NotNull DiagramElement getDiagramElement() {
        return diagramElement;
    }

    /**
     * Gets the sentence number, indexing starts at 1.
     *
     * @return sentence number
     */
    public int getSentenceNo() {
        return sentenceNo;
    }

    public boolean equalEndpoints(DiaTexTraceLink other) {
        return this.sentenceNo == other.getSentenceNo() && diagramElement.equals(other.diagramElement);
    }

    @Override
    public String toString() {
        return getSentence().map(s -> MessageFormat.format("[{0}]-[{1}]", getDiagramElement(), s))
                .orElse(MessageFormat.format("[{0}]-[{1}]", getSentenceNo(), getDiagramElement()));
    }

    public void setText(Text text) {
        this.text = text;
    }

    public Optional<String> getSentence() {
        if (text == null)
            return Optional.empty();
        return Optional.of(text.getSentences().get(getSentenceNo() - 1).getText());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (obj instanceof DiaTexTraceLink other) {
            return Objects.equals(getDiagramElement(), other.getDiagramElement()) && Objects.equals(getSentenceNo(), other.getSentenceNo());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSentenceNo(), getDiagramElement());
    }

    @Override
    public int compareTo(@NotNull DiaTexTraceLink o) {
        if (equals(o))
            return 0;
        var comp = diagramElement.compareTo(o.diagramElement);
        if (comp == 0)
            return sentenceNo - o.getSentenceNo();
        return comp;
    }

    public void setRelated(Set<DiaTexTraceLink> related) {
        Set<DiaTexTraceLink> set = new LinkedHashSet<>(related);
        set.remove(this);
        this.related = set;
    }

    public Set<DiaTexTraceLink> getRelated() {
        return Collections.unmodifiableSet(related);
    }
}
