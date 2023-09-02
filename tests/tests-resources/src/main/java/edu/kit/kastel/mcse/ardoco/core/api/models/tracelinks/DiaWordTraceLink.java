package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * Represents a tracelink between a {@link DiagramElement} and a {@link Word}.
 */
public class DiaWordTraceLink extends DiaTexTraceLink {
    public final static Comparator<DiaWordTraceLink> CONFIDENCE_COMPARATOR = Comparator.comparingDouble(DiaWordTraceLink::getConfidence);

    private final Word word;
    private final double confidence;
    private final Object origin;

    private final Set<DiaWordTraceLink> relatedWordLinks = new TreeSet<>();
    private final Set<DiaGSTraceLink> relatedGSLinks = new TreeSet<>();

    /**
     * Creates a tracelink between a diagram element and a sentence number of a word
     *
     * @param diagramElement diagram element
     * @param word           word
     * @param projectName    project name
     * @param confidence     confidence
     * @param origin         claimant this link was derived from
     */
    public DiaWordTraceLink(@NotNull DiagramElement diagramElement, @NotNull Word word, @NotNull String projectName, double confidence,
            @Nullable Claimant origin) {
        super(diagramElement, word.getSentence(), projectName);

        this.word = word;
        this.confidence = confidence;
        this.origin = origin;
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
            return Objects.equals(getDiagramElement(), other.getDiagramElement()) && getWord().getPosition() == other.getWord().getPosition() && Objects.equals(
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

    public void addRelated(Collection<? extends DiaTexTraceLink> related) {
        var list = new ArrayList<>(related);
        for (var link : list) {
            if (link == this)
                continue;
            if (link instanceof DiaWordTraceLink wLink) {
                relatedWordLinks.add(wLink);
            } else if (link instanceof DiaGSTraceLink gsLink) {
                relatedGSLinks.add(gsLink);
            }
        }
    }

    public Set<DiaGSTraceLink> getRelatedGSLinks() {
        return Collections.unmodifiableSet(relatedGSLinks);
    }

    public Set<DiaWordTraceLink> getRelatedWordLinks() {
        return Collections.unmodifiableSet(relatedWordLinks);
    }

    @Override
    public String toString() {
        var relatedTypes = "";
        if (!relatedGSLinks.isEmpty()) {
            relatedTypes = relatedGSLinks.stream().map(g -> "[" + g.getTraceType().name() + "]").collect(Collectors.joining("-")) + "-";
        }
        return String.format("%s-[%s]-[%s]-[%.3f]-%s", super.toString(false), getWord().getText(), getWord().getPhrase().getText(), getConfidence(),
                relatedTypes);
    }
}
