/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.*;

public class JsonPhrase implements IPhrase, Serializable {
    @JsonProperty
    private int sentenceNumber;
    @JsonProperty
    private String text;
    @JsonProperty
    private PhraseType phraseType;
    @JsonProperty
    private List<Integer> containedWordsIdx;
    @JsonProperty
    private List<JsonPhrase> subPhrases;

    private transient JsonText parent;

    public JsonPhrase() {
        // NOP
    }

    public JsonPhrase(IText source, JsonText parent, IPhrase phrase) {
        this.parent = parent;
        this.sentenceNumber = phrase.getSentenceNo();
        this.text = phrase.getText();
        this.phraseType = phrase.getPhraseType();
        this.containedWordsIdx = phrase.getContainedWords().stream().map(IWord::getPosition).toList();
        this.subPhrases = phrase.getSubPhrases().stream().map(p -> new JsonPhrase(source, parent, p)).toList();
    }

    public void init(JsonText jsonText) {
        this.parent = jsonText;
        this.subPhrases.forEach(p -> p.init(jsonText));
    }

    @Override
    public int getSentenceNo() {
        return sentenceNumber;
    }

    @Override
    public ISentence getSentence() {
        return parent.getSentences().get(sentenceNumber);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public PhraseType getPhraseType() {
        return phraseType;
    }

    @Override
    public List<IWord> getContainedWords() {
        var words = parent.getWords();
        return this.containedWordsIdx.stream().map(words::get).toList();
    }

    @Override
    public List<IPhrase> getSubPhrases() {
        return new ArrayList<>(subPhrases);
    }

    @Override
    public boolean isSuperPhraseOf(IPhrase other) {
        var currText = getText();
        var otherText = other.getText();
        return currText.contains(otherText) && currText.length() != otherText.length();
    }

    @Override
    public boolean isSubPhraseOf(IPhrase other) {
        var currText = getText();
        var otherText = other.getText();
        return otherText.contains(currText) && currText.length() != otherText.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof JsonPhrase that))
            return false;
        return sentenceNumber == that.sentenceNumber && Objects.equals(text, that.text) && phraseType == that.phraseType
                && Objects.equals(containedWordsIdx, that.containedWordsIdx) && Objects.equals(subPhrases, that.subPhrases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceNumber, text, phraseType, containedWordsIdx, subPhrases);
    }
}
