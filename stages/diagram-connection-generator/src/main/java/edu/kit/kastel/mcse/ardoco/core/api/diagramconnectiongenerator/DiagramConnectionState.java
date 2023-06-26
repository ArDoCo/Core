package edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator;

import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public interface DiagramConnectionState extends IConfigurable {
    ImmutableSet<InstanceLink> getDiagramLinks();

    void addToDiagramLinks(RecommendedInstance ri, DiagramElement de, Claimant claimant, double confidence);
}
