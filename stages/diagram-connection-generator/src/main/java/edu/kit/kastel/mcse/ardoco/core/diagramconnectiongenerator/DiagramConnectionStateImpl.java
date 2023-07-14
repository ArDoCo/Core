package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public class DiagramConnectionStateImpl extends AbstractState implements DiagramConnectionState {
    private final Set<DiagramLink> diagramLinks = new HashSet<>();

    @NotNull
    @Override
    public ImmutableSet<DiagramLink> getDiagramLinks() {
        return Sets.immutable.ofAll(diagramLinks);
    }

    @Override
    public boolean addToDiagramLinks(@NotNull RecommendedInstance ri, @NotNull DiagramElement de, @NotNull Claimant claimant, double confidence,
            @NotNull Map<Word, Double> confidenceMap) {
        var newDL = new DiagramLink(ri, de, claimant, confidence, confidenceMap);
        var added = diagramLinks.add(newDL);

        if (added)
            return true;

        for (var dl : diagramLinks) {
            if (dl.equals(newDL)) {
                confidenceMap.forEach((word, conf) -> dl.setConfidence(word, Math.max(dl.getConfidence(word), conf)));
            }
        }
        return false;
    }

    @Override
    public boolean removeFromDiagramLinks(@NotNull DiagramLink diagramLink) {
        return diagramLinks.remove(diagramLink);
    }
}
