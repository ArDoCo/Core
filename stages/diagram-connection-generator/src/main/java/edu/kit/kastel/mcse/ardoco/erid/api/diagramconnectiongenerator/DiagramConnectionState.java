package edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator;

import edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaWordTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public interface DiagramConnectionState extends IConfigurable {
    Logger logger = LoggerFactory.getLogger(DiagramConnectionState.class);

    @Configurable
    double confidenceThreshold = 0.4;

    /**
     * Returns all diagram links.
     *
     * @return immutable set of diagram links
     */
    @NotNull
    ImmutableSet<LinkBetweenDeAndRi> getLinksBetweenDeAndRi();

    default @NotNull Optional<LinkBetweenDeAndRi> getLinkBetweenDeAndRi(@NotNull DiagramElement diagramElement) {
        return getLinksBetweenDeAndRi().stream().filter(d -> d.getDiagramElement().equals(diagramElement)).findFirst();
    }

    default @NotNull ImmutableSet<LinkBetweenDeAndRi> getLinksBetweenDeAndRi(@NotNull RecommendedInstance recommendedInstance) {
        return Sets.immutable.fromStream(getLinksBetweenDeAndRi().stream().filter(d -> d.getRecommendedInstance().equals(recommendedInstance)));
    }

    /**
     * Creates a new diagram link using the specified parameters and adds it to the state. Returns true if a link with the same properties wasn't already
     * contained by the state. The confidenceMap should contain a value for every word covered by this link.
     *
     * @param ri            recommended instance
     * @param de            diagram element
     * @param projectName   name of the project
     * @param claimant      the claimant responsible for the link
     * @param confidenceMap confidence map containing the confidences
     * @return true, if the link wasn't already contained, false else
     */
    boolean addToLinksBetweenDeAndRi(@NotNull RecommendedInstance ri, @NotNull DiagramElement de, @NotNull String projectName, @NotNull Claimant claimant,
            @NotNull Map<Word, Double> confidenceMap);

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

    default @NotNull ImmutableSet<DiaWordTraceLink> getWordTraceLinks() {
        var traceLinks = Sets.mutable.<DiaWordTraceLink>empty();
        for (var linkBetweenDeAndRi : getLinksBetweenDeAndRi()) {
            traceLinks.addAll(linkBetweenDeAndRi.toTraceLinks().toList());
        }
        var aboveThreshold = traceLinks.stream().filter(diaWordTraceLink -> diaWordTraceLink.getConfidence() >= confidenceThreshold).toList();
        logger.info("Removed {} Word Trace Links due to low confidence", traceLinks.size() - aboveThreshold.size());
        return Sets.immutable.ofAll(aboveThreshold);
    }

    default @NotNull ImmutableSet<DiaWordTraceLink> getTraceLinks() {
        return getByEqualDEAndSentence(getWordTraceLinks());
    }

    default @NotNull ImmutableSet<DiaWordTraceLink> getMostSpecificWordTraceLinks() {
        var sameDiagram = getWordTraceLinks().stream().collect(Collectors.groupingBy(tl -> tl.getDiagramElement().getDiagram()));
        var values = sameDiagram.values();
        var allLinks = Sets.mutable.<DiaWordTraceLink>empty();
        for (var diagram : values) {
            var sameWord = diagram.stream().collect(Collectors.groupingBy(tl -> tl.getWord().getPosition()));
            sameWord.remove(-1);
            allLinks.addAll(sameWord.values().stream().flatMap(tls -> getHighestConfidenceTraceLinks(tls).stream()).toList());
        }
        return Sets.immutable.ofAll(allLinks);
    }

    default @NotNull ImmutableSet<DiaWordTraceLink> getMostSpecificTraceLinks() {
        return getByEqualDEAndSentence(getMostSpecificWordTraceLinks());
    }

    private List<DiaWordTraceLink> getHighestConfidenceTraceLinks(@NotNull List<DiaWordTraceLink> traceLinks) {
        var max = traceLinks.stream().max(DiaWordTraceLink.CONFIDENCE_COMPARATOR).map(DiaWordTraceLink::getConfidence).orElse(Double.MAX_VALUE);
        var allMaxima = traceLinks.stream().filter(tl -> Double.compare(tl.getConfidence(), max) >= 0).toList();
        allMaxima.forEach(m -> m.addRelated(allMaxima));
        return allMaxima;
    }

    private ImmutableSet<DiaWordTraceLink> getByEqualDEAndSentence(ImmutableSet<DiaWordTraceLink> links) {
        var sameDEAndSentence = links.groupBy(l -> new Pair<>(l.getDiagramElement(), l.getSentenceNo())).toMap();
        return Sets.immutable.fromStream(sameDEAndSentence.values().stream().map(l -> l.max(DiaWordTraceLink.CONFIDENCE_COMPARATOR)));
    }
}
