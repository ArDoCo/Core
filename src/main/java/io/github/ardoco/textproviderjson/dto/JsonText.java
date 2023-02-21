package io.github.ardoco.textproviderjson.dto;

import com.fasterxml.jackson.annotation.*;
import java.util.List;
import java.util.Objects;

/**
 * A definition of a text
 */
public class JsonText {
    private List<Sentence> sentences;

    /**
     * the words that are contained in this sentence
     */
    @JsonProperty("sentences")
    public List<Sentence> getSentences() { return sentences; }
    @JsonProperty("sentences")
    public void setSentences(List<Sentence> value) { this.sentences = value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonText jsonText = (JsonText) o;
        return Objects.equals(sentences, jsonText.sentences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentences);
    }
}
