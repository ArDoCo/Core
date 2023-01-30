package io.github.ardoco.textproviderjson;

import com.fasterxml.jackson.annotation.*;
import java.util.List;

/**
 * Sentence in a text
 */
public class Sentence {
    private String constituencyTree;
    private long sentenceNo;
    private String text;
    private List<Word> words;

    /**
     * the constituency tree of the sentence in bracket notation
     */
    @JsonProperty("constituencyTree")
    public String getConstituencyTree() { return constituencyTree; }
    @JsonProperty("constituencyTree")
    public void setConstituencyTree(String value) { this.constituencyTree = value; }

    /**
     * index of the sentence
     */
    @JsonProperty("sentenceNo")
    public long getSentenceNo() { return sentenceNo; }
    @JsonProperty("sentenceNo")
    public void setSentenceNo(long value) { this.sentenceNo = value; }

    /**
     * the text of the sentence
     */
    @JsonProperty("text")
    public String getText() { return text; }
    @JsonProperty("text")
    public void setText(String value) { this.text = value; }

    /**
     * the words that are contained in this sentence
     */
    @JsonProperty("words")
    public List<Word> getWords() { return words; }
    @JsonProperty("words")
    public void setWords(List<Word> value) { this.words = value; }
}
