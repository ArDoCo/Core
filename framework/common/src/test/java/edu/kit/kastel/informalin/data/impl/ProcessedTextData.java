/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.data.impl;

import java.util.List;

import edu.kit.kastel.informalin.framework.data.PipelineStepData;

/**
 * Example {@link PipelineStepData}
 */
public class ProcessedTextData implements PipelineStepData {
    private List<String> importantTokens = null;

    public ProcessedTextData() {
        this.importantTokens = null;
    }

    public List<String> getImportantTokens() {
        return importantTokens;
    }

    public void setImportantTokens(List<String> importantTokens) {
        this.importantTokens = importantTokens;
    }
}
