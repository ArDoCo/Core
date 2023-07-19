package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;

public class DiaGSTraceLink extends DiaTexTraceLink {
    private final String goldStandard;

    /**
     * Creates a tracelink between a diagram element and a sentence number
     *
     * @param diagramElement diagram element
     * @param sentenceNo     sentence number, indexing starts at 1
     * @param goldStandard   path to the gold standard file
     */
    public DiaGSTraceLink(@NotNull DiagramElement diagramElement, int sentenceNo, @NotNull String goldStandard) {
        super(diagramElement, sentenceNo);
        this.goldStandard = goldStandard;
    }

    /**
     * {@return the path to the goldstandard text file}
     */
    public @NotNull String getGoldStandard() {
        return goldStandard;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof DiaGSTraceLink other) {
            return Objects.equals(getGoldStandard(), other.getGoldStandard()) && Objects.equals(getDiagramElement(),
                    other.getDiagramElement()) && Objects.equals(getSentenceNo(), other.getSentenceNo());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSentenceNo(), getDiagramElement(), getGoldStandard());
    }

    @Override
    public int compareTo(@NotNull DiaTexTraceLink o) {
        if (equals(o))
            return 0;
        if (o instanceof DiaWordTraceLink other)
            return -1;
        if (o instanceof DiaGSTraceLink other) {
            var gs = 0;
            if (goldStandard == null && other.goldStandard != null)
                gs = 1;
            if (goldStandard != null && other.goldStandard != null)
                gs = goldStandard.compareTo(other.goldStandard);
            if (gs == 0) {
                var comp = diagramElement.compareTo(o.diagramElement);
                if (comp == 0)
                    return getSentenceNo() - o.getSentenceNo();
                return comp;
            }
            return gs;
        }
        return super.compareTo(o);
    }
}
