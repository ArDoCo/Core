package edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaWordTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public class LinkBetweenDeAndRi extends EndpointTuple implements Claimant, Comparable<LinkBetweenDeAndRi> {
    public static final Function<Map<Word, Double>, Double> MAXIMUM_CONFIDENCE = (Map<Word, Double> confidenceMap) -> confidenceMap.values()
            .stream()
            .reduce(0.0, Math::max);
    public static final Function<Map<Word, Double>, Double> MINIMUM_CONFIDENCE = (Map<Word, Double> confidenceMap) -> confidenceMap.values()
            .stream()
            .reduce(0.0, Math::min);
    public static final Function<Map<Word, Double>, Double> AVERAGE_CONFIDENCE = (Map<Word, Double> confidenceMap) -> {
        return confidenceMap.values().stream().reduce(0.0, Double::sum) / confidenceMap.values().size();
    };
    public static final Function<Map<Word, Double>, Double> MEDIAN_CONFIDENCE = (Map<Word, Double> confidenceMap) -> {
        var values = confidenceMap.values().stream().sorted().toList();
        var length = values.size();
        if (length % 2 == 0) {
            return (values.get(length / 2) + values.get(length / 2 - 1)) / 2;
        }
        return values.get(length / 2);
    };

    private final RecommendedInstance recommendedInstance;
    private final DiagramElement diagramElement;
    private final String projectName;
    private final Claimant claimant;
    private final Map<Word, Double> confidenceMap;

    /**
     * @param recommendedInstance the recommended instance
     * @param diagramElement      the diagram element
     * @param projectName         the name of the project
     * @param claimant            the {@link Claimant} responsible for the creation of this link
     * @param confidenceMap       the confidence
     */
    public LinkBetweenDeAndRi(@NotNull RecommendedInstance recommendedInstance, @NotNull DiagramElement diagramElement, @NotNull String projectName,
            @NotNull Claimant claimant, @NotNull Map<Word, Double> confidenceMap) {
        super(recommendedInstance, diagramElement);

        //Assert that confidenceMap is complete
        assert confidenceMap.keySet().containsAll(recommendedInstance.getNameMappings().stream().flatMap(n -> n.getWords().stream()).toList());
        assert new HashSet<>(recommendedInstance.getNameMappings().stream().flatMap(n -> n.getWords().stream()).toList()).size() == confidenceMap.size();

        this.recommendedInstance = recommendedInstance;
        this.diagramElement = diagramElement;
        this.projectName = projectName;
        this.claimant = claimant;
        this.confidenceMap = confidenceMap;
    }

    public RecommendedInstance getRecommendedInstance() {
        return recommendedInstance;
    }

    public DiagramElement getDiagramElement() {
        return diagramElement;
    }

    public double getConfidence(Word word) {
        return confidenceMap.getOrDefault(word, Double.MIN_VALUE);
    }

    public Map<Word, Double> getConfidenceMap() {
        return Map.copyOf(confidenceMap);
    }

    public void setConfidence(Word word, double confidence) {
        confidenceMap.put(word, confidence);
    }

    public double getConfidence(Function<Map<Word, Double>, Double> accumulator) {
        return accumulator.apply(getConfidenceMap());
    }

    @Override
    public String toString() {
        return String.format("[%s]-[%s]-[%s]-[%s]", recommendedInstance.getName(), diagramElement, confidenceMap.values().stream().max(Double::compareTo),
                claimant.getClass().getSimpleName());
    }

    @Override
    public int compareTo(@NotNull LinkBetweenDeAndRi o) {
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
    public @NotNull ImmutableSet<DiaWordTraceLink> toTraceLinks() {
        return Sets.immutable.fromStream(
                getConfidenceMap().entrySet().stream().map(e -> new DiaWordTraceLink(getDiagramElement(), e.getKey(), projectName, e.getValue(), this)));
    }
}
