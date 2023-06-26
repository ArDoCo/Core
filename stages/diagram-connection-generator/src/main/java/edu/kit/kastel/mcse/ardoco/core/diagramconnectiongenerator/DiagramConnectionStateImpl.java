package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public class DiagramConnectionStateImpl extends AbstractState implements DiagramConnectionState {
    private final Set<DiagramLink> diagramLinks = new HashSet<>();

    @Override
    public ImmutableSet<InstanceLink> getDiagramLinks() {
        return Sets.immutable.of();
    }

    @Override
    public void addToDiagramLinks(RecommendedInstance ri, DiagramElement de, Claimant claimant, double confidence) {
        var newDL = new DiagramLink(ri, de, claimant, confidence);
        if (!diagramLinks.contains(newDL)) {
            diagramLinks.add(newDL);
        } else {
            var optionalDiagramLink = diagramLinks.stream().filter(dl -> dl.equals(newDL)).findFirst();
            if (optionalDiagramLink.isEmpty())
                return;
            var existing = optionalDiagramLink.get();
            var newNameMappings = newDL.getRecommendedInstance().getNameMappings();
            var newTypeMappings = newDL.getRecommendedInstance().getTypeMappings();
            existing.getRecommendedInstance().addMappings(newNameMappings, newTypeMappings);
        }
    }
}
