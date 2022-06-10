package edu.kit.kastel.mcse.ardoco.core.api.data.model;

import static edu.kit.kastel.informalin.framework.common.JavaUtils.copyMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.data.IData;

public class ModelStatesData implements IData {
    public static final String ID = "ModelStatesData";

    private Map<String, IModelState> models = new HashMap<>();

    public ModelStatesData() {
        super();
    }

    public ModelStatesData(Map<String, IModelState> models) {
        this.models = models;
    }

    public IModelState getModelState(String id) {
        return models.get(id);
    }

    public void addModelState(String id, IModelState modelState) {
        models.put(id, modelState);
    }

    public Set<String> modelIds() {
        return models.keySet();
    }

    @Override
    public ModelStatesData createCopy() {
        return new ModelStatesData(copyMap(models, IModelState::createCopy));
    }
}
