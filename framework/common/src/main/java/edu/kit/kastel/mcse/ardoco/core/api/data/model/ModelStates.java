/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public class ModelStates implements PipelineStepData {
    public static final String ID = "ModelStatesData";

    private transient Map<String, ModelExtractionState> models = new HashMap<>();

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
    public ModelExtractionState getModelState(String id) {
        return models.get(id);
    }

    /**
     * Adds a {@link ModelExtractionState} with the given id to the set of {@link ModelExtractionState}s
     * 
     * @param id         the id
     * @param modelState the {@link ModelExtractionState}
     */
    public void addModelState(String id, ModelExtractionState modelState) {
        models.put(id, modelState);
    }

    /**
     * Return the set of IDs of all {@link ModelExtractionState}s that are contained within this object.
     * 
     * @return the IDs of all contained {@link ModelExtractionState}s
     */
    public Set<String> modelIds() {
        return models.keySet();
    }

}
