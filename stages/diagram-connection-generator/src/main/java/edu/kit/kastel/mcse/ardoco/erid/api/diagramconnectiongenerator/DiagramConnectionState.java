/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramWordTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.collection.UnmodifiableLinkedHashSet;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi;

/**
 * This state holds the {@link LinkBetweenDeAndRi} trace links. It also provides functions to convert them to {@link DiagramWordTraceLink DiaWordTraceLinks}.
 */
public interface DiagramConnectionState extends IConfigurable {
    Logger logger = LoggerFactory.getLogger(DiagramConnectionState.class);

    /**
     * Returns all diagram links.
     *
     * @return immutable set of diagram links
     */
    @NotNull
    UnmodifiableLinkedHashSet<LinkBetweenDeAndRi> getLinksBetweenDeAndRi();

    /**
     * Returns all diagram links to a specific diagram element.
     *
     * @param diagramElement the element
     * @return a potentially empty set of trace links
     */
    default @NotNull UnmodifiableLinkedHashSet<LinkBetweenDeAndRi> getLinksBetweenDeAndRi(@NotNull DiagramElement diagramElement) {
        return UnmodifiableLinkedHashSet.of(getLinksBetweenDeAndRi().stream().filter(d -> d.getDiagramElement().equals(diagramElement)));
    }

    /**
     * Returns all diagram links to a specific recommended instance.
     *
     * @param recommendedInstance the element
     * @return a potentially empty set of trace links
     */
    default @NotNull UnmodifiableLinkedHashSet<LinkBetweenDeAndRi> getLinksBetweenDeAndRi(@NotNull RecommendedInstance recommendedInstance) {
        return UnmodifiableLinkedHashSet.of(getLinksBetweenDeAndRi().stream().filter(d -> d.getRecommendedInstance().equals(recommendedInstance)));
    }

    /**
     * Creates a new diagram link using the specified parameters and adds it to the state. Returns true if a link with the same properties wasn't already
     * contained by the state. The confidenceMap should contain a value for every word covered by this link.
     *
     * @param recommendedInstance recommended instance
     * @param diagramElement      diagram element
     * @param projectName         name of the project
     * @param claimant            the claimant responsible for the link
     * @param confidenceMap       confidence map containing the confidences
     * @return true, if the link wasn't already contained, false else
     */
    boolean addToLinksBetweenDeAndRi(@NotNull RecommendedInstance recommendedInstance, @NotNull DiagramElement diagramElement, @NotNull String projectName,
            @NotNull Claimant claimant, @NotNull LinkedHashMap<Word, Double> confidenceMap);

    /**
     * Trys to add the diagram link to the state. Returns true if a link with the same properties wasn't already contained by the state.
     *
     * @return true, if the link wasn't already contained, false else
     */
    boolean addToLinksBetweenDeAndRi(@NotNull LinkBetweenDeAndRi linkBetweenDeAndRi);

    /**
     * Removes the specified diagram link from the state. Returns true of the link was contained.
     *
     * @param linkBetweenDeAndRi diagram link
     * @return true, if the link was contained, false else
     */
    boolean removeFromLinksBetweenDeAndRi(@NotNull LinkBetweenDeAndRi linkBetweenDeAndRi);

    /**
     * {@return the confidence threshold for filtering out diagram-to-word trace links}
     */
    double getConfidenceThreshold();

    /**
     * {@return a set of diagram-to-word trace links} A {@link DiagramWordTraceLink} is created for every word covered by a {@link LinkBetweenDeAndRi}. Low
     * confidence links are filtered out.
     */
    default @NotNull UnmodifiableLinkedHashSet<DiagramWordTraceLink> getWordTraceLinks() {
        var traceLinks = new LinkedHashSet<DiagramWordTraceLink>();
        for (var linkBetweenDeAndRi : getLinksBetweenDeAndRi()) {
            traceLinks.addAll(linkBetweenDeAndRi.toTraceLinks());
        }
        var aboveThreshold = traceLinks.stream().filter(diaWordTraceLink -> diaWordTraceLink.getConfidence() >= getConfidenceThreshold()).toList();
        logger.debug("Removed {} Word Trace Links due to low confidence", traceLinks.size() - aboveThreshold.size());
        return UnmodifiableLinkedHashSet.of(aboveThreshold);
    }

    /**
     * {@return a set of diagram-to-sentence trace links} If the sentence is covered by multiple diagram-to-word trace links of the same diagram element, the
     * diagram-to-word trace links are grouped into diagram-to-sentence trace links.
     */
    default @NotNull UnmodifiableLinkedHashSet<DiagramWordTraceLink> getTraceLinks() {
        return getByEqualDEAndSentence(getWordTraceLinks());
    }

    /**
     * {@return a set of diagram-to-word trace links} If multiple trace links point to the same word, the diagram-to-word trace link with the highest confidence
     * is chosen.
     */
    default @NotNull UnmodifiableLinkedHashSet<DiagramWordTraceLink> getMostSpecificWordTraceLinks() {
        var allLinks = new LinkedHashSet<DiagramWordTraceLink>();
        var sameWord = getWordTraceLinks().stream().collect(Collectors.groupingBy(tl -> tl.getWord().getPosition()));
        sameWord.remove(-1);
        allLinks.addAll(sameWord.values().stream().flatMap(tls -> getHighestConfidenceTraceLinks(tls).stream()).toList());
        return UnmodifiableLinkedHashSet.of(allLinks);
    }

    /**
     * {@return a set of diagram-to-sentence trace links} If multiple trace links point to the same word, the diagram-to-word trace link with the highest
     * confidence is chosen. If the sentence is covered by multiple diagram-to-word trace links of the same diagram element, the diagram-to-word trace links are
     * grouped into diagram-to-sentence trace links.
     */
    default @NotNull UnmodifiableLinkedHashSet<DiagramWordTraceLink> getMostSpecificTraceLinks() {
        return getByEqualDEAndSentence(getMostSpecificWordTraceLinks());
    }

    /**
     * {@return the set of diagram-to-word trace links with the highest confidence}
     *
     * @param traceLinks the diagram-to-word trace links
     */
    private List<DiagramWordTraceLink> getHighestConfidenceTraceLinks(@NotNull List<DiagramWordTraceLink> traceLinks) {
        var max = traceLinks.stream().max(DiagramWordTraceLink.CONFIDENCE_COMPARATOR).map(DiagramWordTraceLink::getConfidence).orElse(Double.MAX_VALUE);
        var allMaxima = traceLinks.stream().filter(tl -> Double.compare(tl.getConfidence(), max) >= 0).toList();
        allMaxima.forEach(m -> m.addRelated(allMaxima));
        return allMaxima;
    }

    /**
     * {@return the set of diagram-to-sentence trace links} If a diagram element has multiple diagram-to-word trace links pointing to the same sentence, the
     * sentence with the highest confidence is chosen as representative.
     *
     * @param traceLinks the diagram-to-word trace links
     */
    private UnmodifiableLinkedHashSet<DiagramWordTraceLink> getByEqualDEAndSentence(UnmodifiableLinkedHashSet<DiagramWordTraceLink> traceLinks) {
        var sameDEAndSentence = traceLinks.stream().collect(Collectors.groupingBy(l -> new Pair<>(l.getDiagramElement(), l.getSentenceNo()), LinkedHashMap::new, Collectors.toList()));
        return UnmodifiableLinkedHashSet.of(sameDEAndSentence.values().stream().map(l -> l.stream().max(DiagramWordTraceLink.CONFIDENCE_COMPARATOR).orElseThrow()));
    }
}
