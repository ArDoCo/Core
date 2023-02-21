package io.github.ardoco.textproviderjson.dto;

import com.fasterxml.jackson.annotation.*;
import java.util.List;
import java.util.Objects;

/**
 * A definition of a text
 */
public class TextDTO {
    private List<SentenceDTO> sentences;

    /**
     * the words that are contained in this sentence
     */
    @JsonProperty("sentences")
    public List<SentenceDTO> getSentences() { return sentences; }
    @JsonProperty("sentences")
    public void setSentences(List<SentenceDTO> value) { this.sentences = value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextDTO textDTO = (TextDTO) o;
        return Objects.equals(sentences, textDTO.sentences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentences);
    }
}
