/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * Represents a tracelink between a {@link DiagramElement} and a {@link Word}.
 */
public class DiagramWordTraceLink extends DiagramTextTraceLink {
    public static final Comparator<DiagramWordTraceLink> CONFIDENCE_COMPARATOR = Comparator.comparingDouble(DiagramWordTraceLink::getConfidence);

    private final Word word;
    private final double confidence;
    private final Serializable origin;
    private final TreeSet<DiagramWordTraceLink> relatedWordLinks = new TreeSet<>();
    private final TreeSet<DiagramGoldStandardTraceLink> relatedGSLinks = new TreeSet<>();

    /**
     * Creates a tracelink between a diagram element and a sentence number of a word
     *
     * @param diagramElement diagram element
     * @param word           word
     * @param projectName    project name
     * @param confidence     confidence
     * @param origin         claimant this link was derived from
     */
    public DiagramWordTraceLink(DiagramElement diagramElement, Word word, String projectName, double confidence, Claimant origin) {
        super(diagramElement, word.getSentence(), projectName);

        this.word = word;
        this.confidence = confidence;
        this.origin = origin;
    }

    public Word getWord() {
        return this.word;
    }

    public double getConfidence() {
        return this.confidence;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (obj instanceof DiagramWordTraceLink other) {
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
    public int compareTo(DiagramTextTraceLink o) {
        if (equals(o))
            return 0;
        var supComp = super.compareTo(o);
        if (o instanceof DiagramWordTraceLink other && supComp == 0) {
            var comp = Integer.compare(getWord().getPosition(), other.getWord().getPosition());
            if (comp == 0) {
                return Double.compare(getConfidence(), other.getConfidence());
            }
            return comp;
        }
        return supComp;
    }

    public void addRelated(Collection<? extends DiagramTextTraceLink> related) {
        var list = new ArrayList<>(related);
        for (var link : list) {
            if (link == this)
                continue;
            if (link instanceof DiagramWordTraceLink wLink) {
                relatedWordLinks.add(wLink);
            } else if (link instanceof DiagramGoldStandardTraceLink gsLink) {
                relatedGSLinks.add(gsLink);
            }
        }
    }

    public TreeSet<DiagramGoldStandardTraceLink> getRelatedGSLinks() {
        return relatedGSLinks;
    }

    public TreeSet<DiagramWordTraceLink> getRelatedWordLinks() {
        return relatedWordLinks;
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
