package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.HashMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;

public class PreprocessingData implements IData {

    private IText preprocessedText;
    private Map<String, IModelState> models = new HashMap<>();

    public PreprocessingData(IText preprocessedText) {
        super();
        this.preprocessedText = preprocessedText;
    }

    public void addModel(String id, IModelState modelState) {
        this.models.put(id, modelState);
    }

    @Override
    public IData createCopy() {
        var newInstance = new PreprocessingData(this.preprocessedText);
        newInstance.models = this.models;
        return newInstance;
    }
}
