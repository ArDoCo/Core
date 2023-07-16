package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;

public class DiaTexTraceLink implements Comparable<DiaTexTraceLink>, Serializable {
    protected final DiagramElement diagramElement;
    private final int sentenceNo;

    protected Text text;
    protected Set<DiaTexTraceLink> related = new LinkedHashSet<>();

    private final String goldStandard;

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
        if (text == null)
            return MessageFormat.format("[{0}]-[{1}]", getSentenceNo(), getDiagramElement());
        var sentenceText = text.getSentences().get(getSentenceNo() - 1).getText();
        return MessageFormat.format("[{0}]-[{1}]", getDiagramElement(), sentenceText);
    }

    public void setText(Text text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof DiaTexTraceLink other) {
            var gs = getGoldStandard();
            var oGs = other.getGoldStandard();
            if (Objects.equals(gs, oGs)) {
                var de = getDiagramElement();
                var oDe = other.getDiagramElement();
                if (Objects.equals(de, oDe)) {
                    var sn = getSentenceNo();
                    var oSn = other.getSentenceNo();
                    return Objects.equals(sn, oSn);
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSentenceNo(), getDiagramElement(), getGoldStandard().orElse(""));
    }

    @Override
    public int compareTo(@NotNull DiaTexTraceLink o) {
        if (equals(o))
            return 0;
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

    public void setRelated(Set<DiaTexTraceLink> related) {
        Set<DiaTexTraceLink> set = new LinkedHashSet<>(related);
        set.remove(this);
        this.related = set;
    }

    public Set<DiaTexTraceLink> getRelated() {
        return Collections.unmodifiableSet(related);
    }
}
