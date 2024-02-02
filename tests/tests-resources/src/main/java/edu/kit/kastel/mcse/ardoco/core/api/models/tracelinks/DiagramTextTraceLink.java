/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityComparable;
import edu.kit.kastel.mcse.ardoco.core.data.GlobalConfiguration;

public class DiagramTextTraceLink implements SimilarityComparable<DiagramTextTraceLink>, Comparable<DiagramTextTraceLink>, Serializable {
    protected final DiagramElement diagramElement;
    protected final Sentence sentence;
    protected final String projectName;

    /**
     * Creates a tracelink between a diagram element and a sentence
     *
     * @param diagramElement diagram element
     * @param sentence       sentence
     * @param projectName    project name
     */
    public DiagramTextTraceLink(DiagramElement diagramElement, Sentence sentence, String projectName) {
        this.diagramElement = diagramElement;
        this.sentence = sentence;
        this.projectName = projectName;
    }

    public DiagramElement getDiagramElement() {
        return diagramElement;
    }

    /**
     * Gets the sentence number, indexing starts at 1.
     *
     * @return sentence number
     */
    public int getSentenceNo() {
        return sentence.getSentenceNumberForOutput();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean withSentence) {
        if (withSentence) {
            return MessageFormat.format("[{0}]-[{1}]-[{2}]", getDiagramElement(), getSentence().getSentenceNumberForOutput(), getSentence().getText());
        }
        return MessageFormat.format("[{0}]-[{1}]", getDiagramElement(), getSentence().getSentenceNumberForOutput());
    }

    public Sentence getSentence() {
        return this.sentence;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (obj instanceof DiagramTextTraceLink other) {
            return Objects.equals(getSentenceNo(), other.getSentenceNo()) && Objects.equals(getDiagramElement(), other.getDiagramElement());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSentenceNo(), getDiagramElement());
    }

    @Override
    public int compareTo(DiagramTextTraceLink o) {
        if (equals(o))
            return 0;
        var comp = getDiagramElement().compareTo(o.getDiagramElement());
        if (comp == 0)
            return getSentenceNo() - o.getSentenceNo();
        return comp;
    }

    @Override
    public boolean similar(GlobalConfiguration globalConfiguration, DiagramTextTraceLink obj) {
        if (equals(obj))
            return true;
        return getDiagramElement().getBoundingBox().similar(globalConfiguration, obj.getDiagramElement().getBoundingBox()) && Objects.equals(getSentenceNo(),
                obj.getSentenceNo());
    }
}
