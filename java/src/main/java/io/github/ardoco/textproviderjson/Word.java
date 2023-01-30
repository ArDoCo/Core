package io.github.ardoco.textproviderjson;

import com.fasterxml.jackson.annotation.*;
import java.util.List;

/**
 * Definition of a word
 */
public class Word {
    private long id;
    private List<IncomingDependency> incomingDependencies;
    private String lemma;
    private List<OutgoingDependency> outgoingDependencies;
    private PosTag posTag;
    private long sentenceNo;
    private String text;

    /**
     * The id of the word. Should be ascending from 1 for the first word in the text.
     */
    @JsonProperty("id")
    public long getId() { return id; }
    @JsonProperty("id")
    public void setId(long value) { this.id = value; }

    /**
     * the incoming dependencies
     */
    @JsonProperty("incomingDependencies")
    public List<IncomingDependency> getIncomingDependencies() { return incomingDependencies; }
    @JsonProperty("incomingDependencies")
    public void setIncomingDependencies(List<IncomingDependency> value) { this.incomingDependencies = value; }

    /**
     * the lemma of the word
     */
    @JsonProperty("lemma")
    public String getLemma() { return lemma; }
    @JsonProperty("lemma")
    public void setLemma(String value) { this.lemma = value; }

    /**
     * the outgoing dependencies
     */
    @JsonProperty("outgoingDependencies")
    public List<OutgoingDependency> getOutgoingDependencies() { return outgoingDependencies; }
    @JsonProperty("outgoingDependencies")
    public void setOutgoingDependencies(List<OutgoingDependency> value) { this.outgoingDependencies = value; }

    @JsonProperty("posTag")
    public PosTag getPosTag() { return posTag; }
    @JsonProperty("posTag")
    public void setPosTag(PosTag value) { this.posTag = value; }

    /**
     * index of the sentence the word is contained in
     */
    @JsonProperty("sentenceNo")
    public long getSentenceNo() { return sentenceNo; }
    @JsonProperty("sentenceNo")
    public void setSentenceNo(long value) { this.sentenceNo = value; }

    /**
     * the text of the word
     */
    @JsonProperty("text")
    public String getText() { return text; }
    @JsonProperty("text")
    public void setText(String value) { this.text = value; }
}
