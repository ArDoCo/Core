package io.github.ardoco.textproviderjson;

import com.fasterxml.jackson.annotation.*;
import java.util.List;

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
}
