/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi;

/**
 * @see DiagramConnectionState
 */
public class DiagramConnectionStateImpl extends AbstractState implements DiagramConnectionState {
    @Configurable
    private double confidenceThreshold = 0.4;

    private final LinkedHashSet<LinkBetweenDeAndRi> linksBetweenDeAndRi = new LinkedHashSet<>();

    public DiagramConnectionStateImpl() {
        super();
    }

    @Override
    public Set<LinkBetweenDeAndRi> getLinksBetweenDeAndRi() {
        return new LinkedHashSet<>(linksBetweenDeAndRi);
    }

    @Override
    public boolean addToLinksBetweenDeAndRi(RecommendedInstance recommendedInstance, DiagramElement diagramElement, String textIdentifier, Claimant claimant,
            SortedMap<Word, Double> confidenceMap) {
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
    public boolean addToLinksBetweenDeAndRi(LinkBetweenDeAndRi linkBetweenDeAndRi) {
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
    public boolean removeFromLinksBetweenDeAndRi(LinkBetweenDeAndRi linkBetweenDeAndRi) {
        return linksBetweenDeAndRi.remove(linkBetweenDeAndRi);
    }

    @Override
    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }
}
