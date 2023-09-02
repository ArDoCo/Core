package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;

public class DiaGSTraceLink extends DiaTexTraceLink {
    private final String goldStandard;
    private final TraceType traceType;

    /**
     * Creates a tracelink between a diagram element and a sentence number
     *
     * @param diagramElement diagram element
     * @param sentence       sentence
     * @param projectName    project name
     * @param goldStandard   path to the textual gold standard file
     */
    public DiaGSTraceLink(@NotNull DiagramElement diagramElement, @NotNull Sentence sentence, @NotNull String projectName, @NotNull String goldStandard) {
        this(diagramElement, sentence, projectName, goldStandard, TraceType.ENTITY);
    }

    /**
     * Creates a tracelink between a diagram element and a sentence number
     *
     * @param diagramElement diagram element
     * @param sentence       sentence
     * @param projectName    project name
     * @param goldStandard   path to the textual gold standard file
     * @param traceType      type of the trace
     */
    public DiaGSTraceLink(@NotNull DiagramElement diagramElement, @NotNull Sentence sentence, @NotNull String projectName, @NotNull String goldStandard,
            @NotNull TraceType traceType) {
        super(diagramElement, sentence, projectName);
        this.goldStandard = goldStandard;
        this.traceType = traceType;
    }

    /**
     * {@return the path to the goldstandard text file}
     */
    public @NotNull String getGoldStandard() {
        return goldStandard;
    }

    /**
     * {@return the type of this trace}
     */
    public @NotNull TraceType getTraceType() {
        return traceType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof DiaGSTraceLink other) {
            return Objects.equals(getGoldStandard(), other.getGoldStandard()) && Objects.equals(getDiagramElement(),
                    other.getDiagramElement()) && Objects.equals(getSentenceNo(), other.getSentenceNo()) && Objects.equals(getTraceType(),
                    other.getTraceType());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSentenceNo(), getDiagramElement(), getGoldStandard(), getTraceType());
    }

    @Override
    public int compareTo(@NotNull DiaTexTraceLink o) {
        if (equals(o))
            return 0;
        if (o instanceof DiaWordTraceLink)
            return -1;
        if (o instanceof DiaGSTraceLink other) {
            var gs = 0;
            if (goldStandard == null && other.goldStandard != null)
                gs = 1;
            if (goldStandard != null && other.goldStandard != null)
                gs = goldStandard.compareTo(other.goldStandard);
            if (gs == 0) {
                var comp = diagramElement.compareTo(o.diagramElement);
                if (comp == 0) {
                    var sentenceComp = Integer.compare(getSentenceNo(), o.getSentenceNo());
                    if (sentenceComp == 0) {
                        return getTraceType().compareTo(other.getTraceType());
                    }
                    return sentenceComp;
                }
                return comp;
            }
            return gs;
        }
        return super.compareTo(o);
    }

    @Override
    public String toString() {
        return String.format("%s-[%s]", super.toString(), getTraceType().name());
    }
}
