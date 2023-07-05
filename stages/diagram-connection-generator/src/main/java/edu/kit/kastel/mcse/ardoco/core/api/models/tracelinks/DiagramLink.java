package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.text.MessageFormat;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public class DiagramLink extends EndpointTuple implements Comparable<DiagramLink> {
    private final RecommendedInstance recommendedInstance;
    private final DiagramElement diagramElement;
    private final Claimant claimant;
    private final double confidence;

    /**
     * @param recommendedInstance the recommended instance
     * @param diagramElement      the diagram element
     * @param claimant            the {@link Claimant} responsible for the creation of this link
     * @param confidence          confidence in the link
     */
    public DiagramLink(RecommendedInstance recommendedInstance, DiagramElement diagramElement, Claimant claimant, double confidence) {
        super(recommendedInstance, diagramElement);

        this.recommendedInstance = recommendedInstance;
        this.diagramElement = diagramElement;
        this.claimant = claimant;
        this.confidence = confidence;
    }

    public RecommendedInstance getRecommendedInstance() {
        return recommendedInstance;
    }

    public DiagramElement getDiagramElement() {
        return diagramElement;
    }

    @Override
    public String toString() {
        return MessageFormat.format("[{0}]-[{1}]-[{2}]-[{3}]", recommendedInstance.getName(), diagramElement, confidence, claimant.getClass().getSimpleName());
    }

    @Override
    public int compareTo(@NotNull DiagramLink o) {
        var comp = getRecommendedInstance().getName().compareTo(o.getRecommendedInstance().getName());
        if (comp == 0) {
            return getDiagramElement().compareTo(o.getDiagramElement());
        }
        return comp;
    }
}
