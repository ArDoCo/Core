package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

public class DiaTexTraceLink implements Comparable<DiaTexTraceLink> {
    private final DiagramElement diagramElement;
    private final int sentenceNo;
    private final Word word;
    private final double confidence;

    private Text text;

    private final String goldStandard;

    /**
     * Creates a tracelink between a diagram element and a sentence number of a word
     *
     * @param diagramElement diagram element
     * @param word           word
     * @param confidence     confidence
     */
    public DiaTexTraceLink(@NotNull DiagramElement diagramElement, @NotNull Word word, @NotNull double confidence) {
        this.diagramElement = diagramElement;
        this.goldStandard = null;
        this.sentenceNo = word.getSentenceNo() + 1;
        this.word = word;
        this.confidence = confidence;
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
        this.confidence = 1.0;
    }

    /**
     * Returns an optional containing the path to the goldstandard text file, if this trace link originated from the goldstandard.
     *
     * @return an optional containing the path or empty
     */
    public Optional<String> getGoldStandard() {
        return Optional.ofNullable(goldStandard);
    }

    public @NotNull DiagramElement getDiagramElement() {
        return diagramElement;
    }

    public double getConfidence() {
        return confidence;
    }

    /**
     * Returns an optional containing a word, if this trace link was created from a word.
     *
     * @return an optional containing the word or empty
     */
    public Optional<Word> getWord() {
        return Optional.ofNullable(word);
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
        return this.sentenceNo == other.getSentenceNo() && diagramElement.equals(other.diagramElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceNo, diagramElement, goldStandard);
    }

    @Override
    public String toString() {
        if (text == null)
            return MessageFormat.format("[{0,number,#.###}]-[{1}]-[{2}]", confidence, sentenceNo, diagramElement);
        var sentenceText = text.getSentences().get(getSentenceNo() - 1).getText();
        return MessageFormat.format("[{0,number,#.###}]-[{1}]-[{2}]", confidence, diagramElement, sentenceText);
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
