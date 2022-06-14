package edu.kit.kastel.mcse.ardoco.core.api.data;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;

public class PreprocessingData implements IData {
    public static final String ID = "PreprocessingData";

    private IText preprocessedText;

    public PreprocessingData(IText preprocessedText) {
        super();
        this.preprocessedText = preprocessedText;
    }

    public IText getText() {
        return preprocessedText;
    }

    @Override
    public IData createCopy() {
        var newInstance = new PreprocessingData(this.preprocessedText);
        return newInstance;
    }
}
