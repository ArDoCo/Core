package edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;
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
     * contained by the state.
     *
     * @param ri         recommended instance
     * @param de         diagram element
     * @param claimant   the claimant responsible for the link
     * @param confidence confidence in the link
     * @return true, if the link wasn't already contained, false else
     */
    boolean addToDiagramLinks(@NotNull RecommendedInstance ri, @NotNull DiagramElement de, @NotNull Claimant claimant, double confidence);

    /**
     * Removes the specified diagram link from the state. Returns true of the link was contained.
     *
     * @param diagramLink diagram link
     * @return true, if the link was contained, false else
     */
    boolean removeFromDiagramLinks(@NotNull DiagramLink diagramLink);

    default @NotNull ImmutableSet<DiaTexTraceLink> getTraceLinks() {
        var traceLinks = Lists.mutable.<DiaTexTraceLink>empty();
        for (var diagramLink : getDiagramLinks()) {
            var tls = diagramLink.toTraceLinks().toList();
            traceLinks.addAll(tls);
        }
        return Sets.immutable.ofAll(traceLinks);
    }

    default @NotNull ImmutableSet<DiaTexTraceLink> getMostSpecificTraceLinks() {
        var sameDiagram = getTraceLinks().stream().collect(Collectors.groupingBy(tl -> tl.getDiagramElement().getDiagram()));
        var values = sameDiagram.values();
        var allLinks = Sets.mutable.<DiaTexTraceLink>empty();
        for (var diagram : values) {
            var sameWord = diagram.stream().collect(Collectors.groupingBy(tl -> tl.getWord().map(Word::getPosition).orElse(-1)));
            sameWord.remove(-1);
            allLinks.addAll(sameWord.values().stream().map(this::getMostSpecificTraceLink).flatMap(Optional::stream).toList());
        }
        return allLinks.toImmutable();
    }

    private Optional<DiaTexTraceLink> getMostSpecificTraceLink(@NotNull List<DiaTexTraceLink> traceLinks) {
        return traceLinks.stream().max(Comparator.comparingDouble(DiaTexTraceLink::getConfidence));
    }
}
