/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramWordTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * This class represents a trace link between a {@link DiagramElement} and a {@link RecommendedInstance}.
 */
public class LinkBetweenDeAndRi extends EndpointTuple implements Claimant, Comparable<LinkBetweenDeAndRi> {
    private final RecommendedInstance recommendedInstance;
    private final DiagramElement diagramElement;
    private final String projectName;
    private final Claimant claimant;
    private final SortedMap<Word, Double> confidenceMap;

    /**
     * Creates a new trace link for the given project with the confidence map provided by the claimant.
     *
     * @param recommendedInstance the recommended instance
     * @param diagramElement      the diagram element
     * @param projectName         the name of the project
     * @param claimant            the {@link Claimant} responsible for the creation of this link
     * @param confidenceMap       the confidence
     */
    public LinkBetweenDeAndRi(RecommendedInstance recommendedInstance, DiagramElement diagramElement, String projectName, Claimant claimant,
            SortedMap<Word, Double> confidenceMap) {
        super(recommendedInstance, diagramElement);

        this.recommendedInstance = recommendedInstance;
        this.diagramElement = diagramElement;
        this.projectName = projectName;
        this.claimant = claimant;
        this.confidenceMap = confidenceMap;
    }

    /**
     * {@return the traced recommended instance}
     */
    public RecommendedInstance getRecommendedInstance() {
        return recommendedInstance;
    }

    /**
     * {@return the traced diagram element}
     */
    public DiagramElement getDiagramElement() {
        return diagramElement;
    }

    /**
     * {@return the confidence for a given word} {@link Double#MIN_VALUE}, if the word is not part of the traced recommended instance.
     *
     * @param word the word
     */
    public double getConfidence(Word word) {
        return confidenceMap.getOrDefault(word, Double.MIN_VALUE);
    }

    /**
     * {@return a map of confidences for each word that is part of the recommended instance}
     */
    public SortedMap<Word, Double> getConfidenceMap() {
        return new TreeMap<>(confidenceMap);
    }

    /**
     * Sets the confidence for a specific word. Does not check whether the word is contained by the recommended instance.
     */
    public void setConfidence(Word word, double confidence) {
        confidenceMap.put(word, confidence);
    }

    /**
     * {@return the overall confidence in the link} The confidence is calculated by aggregating the confidence map values using a {@link AggregationFunctions}.
     *
     * @param aggregationFunction an aggregation function
     */
    public double getConfidence(AggregationFunctions aggregationFunction) {
        return aggregationFunction.applyAsDouble(getConfidenceMap().values());
    }

    @Override
    public String toString() {
        return String.format("[%s]-[%s]-[%s]-[%s]", recommendedInstance.getName(), diagramElement, confidenceMap.values().stream().max(Double::compareTo),
                claimant.getClass().getSimpleName());
    }

    @Override
    public int compareTo(LinkBetweenDeAndRi o) {
        if (equals(o))
            return 0;
        var comp = getRecommendedInstance().getName().compareTo(o.getRecommendedInstance().getName());
        if (comp == 0) {
            return getDiagramElement().compareTo(o.getDiagramElement());
        }
        return comp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LinkBetweenDeAndRi other) {
            return diagramElement.equals(other.diagramElement) && recommendedInstance.equals(other.recommendedInstance) && claimant.equals(other.claimant);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(diagramElement, recommendedInstance, claimant);
    }

    /**
     * Returns an immutable set of tracelinks between the diagram element of this link and every word that is contained in the recommended instance's name
     * mappings. Can be empty.
     *
     * @return immutable set of diagram text tracelinks
     */
    public Set<DiagramWordTraceLink> toTraceLinks() {
        return new LinkedHashSet<>(getConfidenceMap().entrySet()
                .stream()
                .map(e -> new DiagramWordTraceLink(getDiagramElement(), e.getKey(), projectName, e.getValue(), this))
                .toList());
    }
}
