/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public class ModelStates implements PipelineStepData {
    public static final String ID = "ModelStatesData";

    private transient Map<String, ModelExtractionState> modelExtractionStates = new HashMap<>();
    private transient Map<String, Model> models = new HashMap<>();

    /**
     * Constructor to create a {@link ModelStates} object that holds all {@link ModelExtractionState}s
     */
    public ModelStates() {
        super();
    }

    /**
     * Returns the {@link ModelExtractionState} with the given id
     *
     * @param id the id
     * @return the corresponding {@link ModelExtractionState}
     */
    public ModelExtractionState getModelExtractionState(String id) {
        return modelExtractionStates.get(id);
    }

    /**
     * Adds a {@link ModelExtractionState} with the given id to the set of {@link ModelExtractionState}s
     *
     * @param id         the id
     * @param modelState the {@link ModelExtractionState}
     */
    public void addModelExtractionState(String id, ModelExtractionState modelState) {
        modelExtractionStates.put(id, modelState);
    }

    /**
     * Return the set of IDs of all {@link ModelExtractionState ModelExtractionStates} that are contained within this object.
     *
     * @return the IDs of all contained {@link ModelExtractionState ModelExtractionStates}
     */
    public Set<String> extractionModelIds() {
        return modelExtractionStates.keySet();
    }

    /**
     * Return the set of IDs of all {@link Model Models} that are contained within this object.
     *
     * @return the IDs of all contained {@link Model Models}
     */
    public Set<String> modelIds() {
        return models.keySet();
    }

    /**
     * Adds a {@link Model} with the given id to the set of {@link Model Models}
     *
     * @param id    the id
     * @param model the {@link Model}
     */
    public void addModel(String id, Model model) {
        models.put(id, model);
    }

    /**
     * Returns the {@link Model} with the given id
     *
     * @param id the id
     * @return the corresponding {@link Model}
     */
    public Model getModel(String id) {
        return models.get(id);
    }

}
