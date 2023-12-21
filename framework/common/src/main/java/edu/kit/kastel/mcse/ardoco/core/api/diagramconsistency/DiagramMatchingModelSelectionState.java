/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.ElementRole;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.WeightedTextSimilarity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Stores data created during the model selection process, as well as the selection itself. The data includes a created
 * text similarity function and all occurrences of diagram elements in models.
 */
@Deterministic
public interface DiagramMatchingModelSelectionState extends PipelineStepData {
    /**
     * The ID of this state.
     */
    String ID = "DiagramMatchingModelSelectionState";

    /**
     * Gets the model types that are in principle available, meaning their loading is attempted.
     *
     * @return The model types.
     */
    Set<ModelType> getAvailableModelTypes();

    /**
     * Sets the model types that are in principle available, meaning their loading is attempted.
     *
     * @param availableModelTypes
     *                            The model types.
     */
    void setAvailableModelTypes(Set<ModelType> availableModelTypes);

    /**
     * Gets the similarity function.
     *
     * @return The similarity function.
     */
    WeightedTextSimilarity getSimilarityFunction();

    /**
     * Sets the similarity function.
     *
     * @param similarity
     *                   The similarity function.
     */
    void setSimilarityFunction(WeightedTextSimilarity similarity);

    /**
     * Gets the model type that is selected to be matched with the diagram.
     *
     * @return The model type.
     */
    Set<ModelType> getSelection();

    /**
     * Sets the models that are selected to be matched with the diagram.
     *
     * @param modelTypes
     *                   The model types.
     */
    void setSelection(Set<ModelType> modelTypes);

    /**
     * Adds an occurrence of a diagram element in a model.
     *
     * @param diagramID The ID of the diagram element.
     * @param modelType The model the model element is in.
     * @param modelID   The ID of the model element.
     * @param role      The role of the model element.
     */
    void addOccurrence(String diagramID, ModelType modelType, String modelID, ElementRole role);

    /**
     * Get all occurrences of a diagram element in a model.
     *
     * @param diagramID The ID of the diagram element.
     * @param modelType The model to get the occurrences in.
     * @return The occurrences.
     */
    List<Occurrence> getOccurrences(String diagramID, ModelType modelType);

    /**
     * Get all occurrences of a diagram element in all models.
     *
     * @param diagramID
     *                  The ID of the diagram element.
     * @return The occurrences.
     */
    List<Occurrence> getOccurrences(String diagramID);

    /**
     * Gets the explanation why the model type was selected.
     *
     * @return The explanation, which is a match value for each model type.
     */
    Map<ModelType, Double> getSelectionExplanation();

    /**
     * Sets the explanation why the model type was selected.
     *
     * @param explanation
     *                    The explanation, which is a match value for each model type.
     */
    void setSelectionExplanation(Map<ModelType, Double> explanation);

    /**
     * Describes an occurrence of a diagram element in a model.
     *
     * @param modelID
     *                The ID of the model element.
     * @param role
     *                The role of the model element.
     */
    public record Occurrence(String modelID, ElementRole role) {
    }
}
