package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.text.MessageFormat;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

public class DiaTexTraceLink implements Comparable<DiaTexTraceLink> {
    private final DiagramElement diagramElement;
    private final int sentenceNo;
    private final Word word;

    private Text text;

    private final String goldStandard;

    /**
     * Creates a tracelink between a diagram element and a sentence number of a word
     *
     * @param diagramElement diagram element
     * @param word           word
     */
    public DiaTexTraceLink(@NotNull DiagramElement diagramElement, @NotNull Word word) {
        this.diagramElement = diagramElement;
        this.goldStandard = null;
        this.sentenceNo = word.getSentenceNo() + 1;
        this.word = word;
    }

    /**
     * Creates a tracelink between a diagram element and a sentence number
     *
     * @param diagramElement diagram element
     * @param sentenceNo     sentence number, indexing starts at 1
     * @param goldStandard   path to the gold standard file
     */
    public DiaTexTraceLink(@NotNull DiagramElement diagramElement, int sentenceNo, @Nullable String goldStandard) {
        this.diagramElement = diagramElement;
        this.goldStandard = goldStandard;
        this.sentenceNo = sentenceNo;
        this.word = null;
    }

    /**
     * Returns the path to the goldstandard text file, if this trace link originated from the goldstandard.
     *
     * @return path or null
     */
    public @Nullable String getGoldStandard() {
        return goldStandard;
    }

    public @NotNull DiagramElement getDiagramElement() {
        return diagramElement;
    }

    public @Nullable Word getWord() {
        return word;
    }

    /**
     * Gets the sentence number, indexing starts at 1.
     *
     * @return sentence number
     */
    public int getSentenceNo() {
        return sentenceNo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiaTexTraceLink other) {
            if (this.goldStandard != null && other.goldStandard != null && !this.goldStandard.equals(other.goldStandard))
                return false;
            return equalEndpoints(other);
        }
        return false;
    }

    public boolean equalEndpoints(DiaTexTraceLink other) {
        return this.sentenceNo == other.getSentenceNo() && diagramElement.equals(other.getDiagramElement());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceNo, diagramElement, goldStandard);
    }

    @Override
    public String toString() {
        if (text == null)
            return MessageFormat.format("[{0}]-[{1}]", sentenceNo, diagramElement);
        var sentenceText = text.getSentences().get(getSentenceNo() - 1).getText();
        return MessageFormat.format("[{0}]-[{1}]", getDiagramElement(), sentenceText);
    }

    public void setText(Text text) {
        this.text = text;
    }

    @Override
    public int compareTo(@NotNull DiaTexTraceLink o) {
        var gs = 0;
        if (goldStandard != null && o.goldStandard == null)
            gs = -1;
        if (goldStandard == null && o.goldStandard != null)
            gs = 1;
        if (goldStandard != null && o.goldStandard != null)
            gs = goldStandard.compareTo(o.goldStandard);
        if (gs == 0) {
            var comp = diagramElement.compareTo(o.diagramElement);
            if (comp == 0)
                return sentenceNo - o.getSentenceNo();
            return comp;
        }
        return gs;
    }
}
