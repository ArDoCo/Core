/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency;

import java.util.*;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.ElementRole;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.WeightedTextSimilarity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;

/**
 * Implementation of {@link DiagramMatchingModelSelectionState}.
 */
public class DiagramMatchingModelSelectionStateImpl implements DiagramMatchingModelSelectionState {

    private final Map<ModelType, Map<String, List<Occurrence>>> occurrences = new LinkedHashMap<>();
    private transient Set<ModelType> availableModelTypes = null;
    private transient Set<ModelType> selectedModelTypes = null;
    private transient Map<ModelType, Double> explanation = null;
    private transient WeightedTextSimilarity weightedTextSimilarity;

    @Override
    public Set<ModelType> getAvailableModelTypes() {
        return this.availableModelTypes;
    }

    @Override
    public void setAvailableModelTypes(Set<ModelType> availableModelTypes) {
        this.availableModelTypes = Collections.unmodifiableSet(new LinkedHashSet<>(availableModelTypes));
    }

    @Override
    public WeightedTextSimilarity getSimilarityFunction() {
        return this.weightedTextSimilarity;
    }

    @Override
    public void setSimilarityFunction(WeightedTextSimilarity similarity) {
        this.weightedTextSimilarity = similarity;
    }

    @Override
    public Set<ModelType> getSelection() {
        return this.selectedModelTypes;
    }

    @Override
    public void setSelection(Set<ModelType> modelTypes) {
        this.selectedModelTypes = Collections.unmodifiableSet(new LinkedHashSet<>(modelTypes));
    }

    @Override
    public void addOccurrence(String diagramID, ModelType modelType, String modelID, ElementRole role) {
        this.occurrences.computeIfAbsent(modelType, k -> new TreeMap<>()).computeIfAbsent(diagramID, k -> new ArrayList<>()).add(new Occurrence(modelID, role));
    }

    @Override
    public List<Occurrence> getOccurrences(String diagramID, ModelType modelType) {
        var occurrencesPerBox = this.occurrences.get(modelType);

        if (occurrencesPerBox == null) {
            return List.of();
        }

        var requestedOccurrences = occurrencesPerBox.get(diagramID);

        return Objects.requireNonNullElse(requestedOccurrences, List.of());
    }

    @Override
    public List<Occurrence> getOccurrences(String diagramID) {
        var allOccurrences = new ArrayList<Occurrence>();
        for (var occurrencesPerBox : this.occurrences.values()) {
            var requestedOccurrences = occurrencesPerBox.get(diagramID);
            if (requestedOccurrences != null) {
                allOccurrences.addAll(requestedOccurrences);
            }
        }
        return allOccurrences;
    }

    @Override
    public Map<ModelType, Double> getSelectionExplanation() {
        return this.explanation;
    }

    @Override
    public void setSelectionExplanation(Map<ModelType, Double> explanation) {
        this.explanation = explanation;
    }
}
