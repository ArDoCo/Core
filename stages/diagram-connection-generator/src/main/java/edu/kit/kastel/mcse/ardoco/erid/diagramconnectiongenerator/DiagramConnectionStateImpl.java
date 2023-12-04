/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.collection.UnmodifiableLinkedHashSet;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
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

    public DiagramConnectionStateImpl(DataRepository dataRepository) {
        super(dataRepository);
    }

    @NotNull
    @Override
    public UnmodifiableLinkedHashSet<LinkBetweenDeAndRi> getLinksBetweenDeAndRi() {
        return UnmodifiableLinkedHashSet.of(linksBetweenDeAndRi);
    }

    @Override
    public boolean addToLinksBetweenDeAndRi(@NotNull RecommendedInstance recommendedInstance, @NotNull DiagramElement diagramElement,
            @NotNull String textIdentifier, @NotNull Claimant claimant, @NotNull LinkedHashMap<Word, Double> confidenceMap) {
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

    @Override
    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }
}
