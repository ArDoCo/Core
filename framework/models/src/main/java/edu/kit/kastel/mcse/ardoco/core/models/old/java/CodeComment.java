/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.old.java;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeComment implements Serializable {
    @JsonProperty
    private String type;
    @JsonProperty
    private String text;
    @JsonProperty
    private int lineNumber;
    @JsonProperty
    private boolean isOrphan;

    CodeComment() {
        // Jackson
    }

    CodeComment(String type, String text, int lineNumber, boolean isOrphan) {
        this.type = type;
        this.text = text;
        this.lineNumber = lineNumber;
        this.isOrphan = isOrphan;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @return the lineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @return the isOrphan
     */
    public boolean isOrphan() {
        return isOrphan;
    }

    @Override
    public String toString() {
        return lineNumber + "|" + type + "|" + isOrphan + "|" + text.replace("\\n", "").trim();
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, text);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CodeComment other)) {
            return false;
        }
        return lineNumber == other.lineNumber && Objects.equals(text, other.text);
    }
}
