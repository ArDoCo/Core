package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;

public class PreprocessingData implements IData {
    public static final String ID = "PreprocessingData";

    private IText preprocessedText;
    private Map<String, IModelState> models = new HashMap<>();

    public PreprocessingData(IText preprocessedText) {
        super();
        this.preprocessedText = preprocessedText;
    }

    public void addModel(String id, IModelState modelState) {
        this.models.put(id, modelState);
    }

    public IText getText() {
        return preprocessedText;
    }

    public Set<String> getModelIds() {
        return models.keySet();
    }

    public List<IModelState> getModels() {
        return models.values().stream().toList();
    }

    public IModelState getModel(String id) {
        return models.get(id);
    }

    @Override
    public IData createCopy() {
        var newInstance = new PreprocessingData(this.preprocessedText);
        newInstance.models = this.models;
        return newInstance;
    }
}
