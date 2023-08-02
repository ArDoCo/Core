package edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaWordTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public interface DiagramConnectionState extends IConfigurable {
    /**
     * Returns all diagram links.
     *
     * @return immutable set of diagram links
     */
    @NotNull
    ImmutableSet<DiagramLink> getDiagramLinks();

    default @NotNull Optional<DiagramLink> getDiagramLink(@NotNull DiagramElement diagramElement) {
        return getDiagramLinks().stream().filter(d -> d.getDiagramElement().equals(diagramElement)).findFirst();
    }

    /**
     * Creates a new diagram link using the specified parameters and adds it to the state. Returns true if a link with the same properties wasn't already
     * contained by the state. The confidenceMap should contain a value for every word covered by this link.
     *
     * @param ri            recommended instance
     * @param de            diagram element
     * @param projectName   name of the project
     * @param claimant      the claimant responsible for the link
     * @param confidence    confidence in the link
     * @param confidenceMap confidence map containing the confidences
     * @return true, if the link wasn't already contained, false else
     */
    boolean addToDiagramLinks(@NotNull RecommendedInstance ri, @NotNull DiagramElement de, @NotNull String projectName, @NotNull Claimant claimant,
            double confidence, @NotNull Map<Word, Double> confidenceMap);

    /**
     * Trys to add the diagram link to the state. Returns true if a link with the same properties wasn't already contained by the state.
     *
     * @return true, if the link wasn't already contained, false else
     */
    boolean addToDiagramLinks(@NotNull DiagramLink diagramLink);

    /**
     * Removes the specified diagram link from the state. Returns true of the link was contained.
     *
     * @param diagramLink diagram link
     * @return true, if the link was contained, false else
     */
    boolean removeFromDiagramLinks(@NotNull DiagramLink diagramLink);

    default @NotNull ImmutableSet<DiaWordTraceLink> getWordTraceLinks() {
        var traceLinks = Sets.mutable.<DiaWordTraceLink>empty();
        for (var diagramLink : getDiagramLinks()) {
            traceLinks.addAll(diagramLink.toTraceLinks().toList());
        }
        return traceLinks.toImmutable();
    }

    default @NotNull ImmutableSet<DiaWordTraceLink> getTraceLinks() {
        return getByEqualEndpoints(getWordTraceLinks());
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
        return getByEqualEndpoints(getMostSpecificWordTraceLinks());
    }

    private List<DiaWordTraceLink> getHighestConfidenceTraceLinks(@NotNull List<DiaWordTraceLink> traceLinks) {
        var sorted = traceLinks.stream().sorted((t1, t2) -> Double.compare(t2.getConfidence(), t1.getConfidence())).toList();
        var max = sorted.stream().findFirst().map(DiaWordTraceLink::getConfidence).orElse(Double.MAX_VALUE);
        sorted.forEach(tl -> tl.addRelated(sorted));
        return sorted.stream().filter(tl -> Double.compare(tl.getConfidence(), max) >= 0).toList();
    }

    private ImmutableSet<DiaWordTraceLink> getByEqualEndpoints(ImmutableSet<DiaWordTraceLink> links) {
        var list = new ArrayList<DiaWordTraceLink>();
        for (var link : links) {
            if (list.stream().anyMatch(l -> l.equalEndpoints(link)))
                continue;
            list.add(link);
        }
        return Sets.immutable.ofAll(list);
    }
}
