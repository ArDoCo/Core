/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.data.impl;

import java.util.List;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Example {@link PipelineStepData}
 */
public class TextData implements PipelineStepData {
    private final String text;
    private List<String> tokens;

    public TextData(String text) {
        this.text = text;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TextData textData = (TextData) o;

        return Objects.equals(text, textData.text);
    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }
}
