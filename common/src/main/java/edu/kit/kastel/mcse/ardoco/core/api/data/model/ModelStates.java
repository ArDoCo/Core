/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.informalin.data.PipelineStepData;

public class ModelStates implements PipelineStepData {
    public static final String ID = "ModelStatesData";

    private transient Map<String, ModelExtractionState> models = new HashMap<>();

    public ModelStates() {
        super();
    }

    public ModelExtractionState getModelState(String id) {
        return models.get(id);
    }

    public void addModelState(String id, ModelExtractionState modelState) {
        models.put(id, modelState);
    }

    public Set<String> modelIds() {
        return models.keySet();
    }

}
