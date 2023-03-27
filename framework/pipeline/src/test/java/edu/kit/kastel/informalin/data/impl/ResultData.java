/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.data.impl;

import edu.kit.kastel.informalin.data.PipelineStepData;

/**
 * Example {@link PipelineStepData}
 */
public class ResultData implements PipelineStepData {
    private String result = null;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
