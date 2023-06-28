package edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public interface DiagramConnectionState extends IConfigurable {
    ImmutableSet<DiagramLink> getDiagramLinks();

    void addToDiagramLinks(RecommendedInstance ri, DiagramElement de, Claimant claimant, double confidence);

    default ImmutableSet<DiaTexTraceLink> getTraceLinks() {
        MutableSet<DiaTexTraceLink> traceLinks = Sets.mutable.empty();
        for (var diagramLink : getDiagramLinks()) {
            for (var nameMapping : diagramLink.getRecommendedInstance().getNameMappings()) {
                for (var word : nameMapping.getWords()) {
                    var traceLink = new DiaTexTraceLink(diagramLink.getDiagramElement(), word);
                    traceLinks.add(traceLink);
                }
            }
        }
        return Sets.immutable.ofAll(traceLinks);
    }
}
