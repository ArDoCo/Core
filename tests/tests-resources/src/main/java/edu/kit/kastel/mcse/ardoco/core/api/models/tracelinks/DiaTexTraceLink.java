package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.text.MessageFormat;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

public class DiaTexTraceLink {
    private final DiagramElement diagramElement;
    private final int sentenceNo;
    private final Word word;

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
        //TODO I do not like that the information where indexing start isn't explicit, maybe add a wrapper, enum, or sth.
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

    public int getSentenceNo() {
        return sentenceNo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiaTexTraceLink other) {
            return this.sentenceNo == other.getSentenceNo() && diagramElement.equals(other.getDiagramElement());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceNo, diagramElement);
    }

    @Override
    public String toString() {
        return MessageFormat.format("[{0}]-[{1}]", sentenceNo, diagramElement);
    }
}
