/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import edu.kit.kastel.informalin.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;

public class PreprocessingData implements PipelineStepData {
    public static final String ID = "PreprocessingData";

    private transient Text preprocessedText;

    public PreprocessingData(Text preprocessedText) {
        super();
        this.preprocessedText = preprocessedText;
    }

    public Text getText() {
        return preprocessedText;
    }

}
