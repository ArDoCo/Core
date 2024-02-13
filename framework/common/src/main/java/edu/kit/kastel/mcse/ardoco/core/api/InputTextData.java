/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public class InputTextData implements PipelineStepData {

    public static final String ID = "InputTextData";

    private String text;

    public InputTextData(String text) {
        super();
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
