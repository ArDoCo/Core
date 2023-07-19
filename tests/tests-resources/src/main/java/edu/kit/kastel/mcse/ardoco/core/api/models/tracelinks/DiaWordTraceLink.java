package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.text.MessageFormat;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * Represents a tracelink between a {@link DiagramElement} and a {@link Word}.
 */
public class DiaWordTraceLink extends DiaTexTraceLink {
    private final Word word;
    private final double confidence;

    /**
     * Creates a tracelink between a diagram element and a sentence number of a word
     *
     * @param diagramElement diagram element
     * @param word           word
     * @param projectName    project name
     * @param confidence     confidence
     */
    public DiaWordTraceLink(@NotNull DiagramElement diagramElement, @NotNull Word word, @NotNull String projectName, double confidence) {
        super(diagramElement, word.getSentence(), projectName);

        this.word = word;
        this.confidence = confidence;
    }

    public @NotNull Word getWord() {
        return this.word;
    }

    public double getConfidence() {
        return this.confidence;
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (this == obj)
            return true;
        else if (obj instanceof DiaWordTraceLink other) {
            return Objects.equals(getDiagramElement(), other.getDiagramElement()) && Objects.equals(getWord(), other.getWord()) && Objects.equals(
                    getConfidence(), other.getConfidence());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDiagramElement(), getWord(), getConfidence());
    }

    @Override
    public int compareTo(@NotNull DiaTexTraceLink o) {
        if (equals(o))
            return 0;
        var supComp = super.compareTo(o);
        if (o instanceof DiaWordTraceLink other) {
            if (supComp == 0) {
                var comp = Integer.compare(getWord().getPosition(), other.getWord().getPosition());
                if (comp == 0) {
                    return Double.compare(getConfidence(), other.getConfidence());
                }
                return comp;
            }
        }
        return supComp;
    }

    @Override
    public String toString() {
        return MessageFormat.format("[{0}]-[{1}]-[{2}]-[{3,number,#.###}]", getDiagramElement(), getSentence().getText(), getWord().getPosition(),
                getConfidence());
    }
}
