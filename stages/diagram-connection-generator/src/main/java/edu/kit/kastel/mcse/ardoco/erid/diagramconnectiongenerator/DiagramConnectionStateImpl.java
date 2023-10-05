package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator;

import edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionState;

/**
 * @see DiagramConnectionState
 */
public class DiagramConnectionStateImpl extends AbstractState implements DiagramConnectionState {
    private final Set<LinkBetweenDeAndRi> linksBetweenDeAndRi = new HashSet<>();

    @NotNull
    @Override
    public ImmutableSet<LinkBetweenDeAndRi> getLinksBetweenDeAndRi() {
        return Sets.immutable.ofAll(linksBetweenDeAndRi);
    }

    @Override
    public boolean addToLinksBetweenDeAndRi(@NotNull RecommendedInstance recommendedInstance, @NotNull DiagramElement diagramElement, @NotNull String textIdentifier, @NotNull Claimant claimant,
                                            @NotNull Map<Word, Double> confidenceMap) {
        var newDL = new LinkBetweenDeAndRi(recommendedInstance, diagramElement, textIdentifier, claimant, confidenceMap);
        var added = linksBetweenDeAndRi.add(newDL);

        if (added)
            return true;

        for (var dl : linksBetweenDeAndRi) {
            if (dl.equals(newDL)) {
                confidenceMap.forEach((word, conf) -> dl.setConfidence(word, Math.max(dl.getConfidence(word), conf)));
            }
        }
        return false;
    }

    @Override
    public boolean addToLinksBetweenDeAndRi(@NotNull LinkBetweenDeAndRi linkBetweenDeAndRi) {
        var added = linksBetweenDeAndRi.add(linkBetweenDeAndRi);

        if (added)
            return true;

        for (var dl : linksBetweenDeAndRi) {
            if (dl.equals(linkBetweenDeAndRi)) {
                linkBetweenDeAndRi.getConfidenceMap().forEach((word, conf) -> dl.setConfidence(word, Math.max(dl.getConfidence(word), conf)));
            }
        }
        return false;
    }

    @Override
    public boolean removeFromLinksBetweenDeAndRi(@NotNull LinkBetweenDeAndRi linkBetweenDeAndRi) {
        return linksBetweenDeAndRi.remove(linkBetweenDeAndRi);
    }
}
