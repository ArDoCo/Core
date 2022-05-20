/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;

public class JsonSentence implements ISentence, Serializable {

    @Serial
    private static final long serialVersionUID = 7633202824798737860L;

    @JsonProperty
    private int sentenceNumber;
    @JsonProperty
    private String text;
    @JsonProperty
    private int startIndexInclude;
    @JsonProperty
    private int endIndexInclude;
    @JsonProperty
    private List<JsonPhrase> phrases;

    private transient JsonText parent;

    public JsonSentence() {
        // NOP
    }

    public JsonSentence(IText source, JsonText parent, ISentence sentence) {
        this.parent = parent;
        this.sentenceNumber = sentence.getSentenceNumber();
        this.text = sentence.getText();

        var firstWord = sentence.getWords().getFirstOptional();
        var lastWord = sentence.getWords().getLastOptional();
        if (firstWord.isEmpty() || lastWord.isEmpty()) {
            throw new IllegalArgumentException("A sentence has must have a word!");
        }
        this.startIndexInclude = source.getWords().indexOf(firstWord.get());
        this.endIndexInclude = source.getWords().indexOf(lastWord.get());

        this.phrases = sentence.getPhrases().stream().map(p -> new JsonPhrase(source, parent, p)).toList();
    }

    public void init(JsonText jsonText) {
        this.parent = jsonText;
        this.phrases.forEach(p -> p.init(jsonText));
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public ImmutableList<IWord> getWords() {
        return parent.getWords().subList(startIndexInclude, endIndexInclude + 1);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public ImmutableList<IPhrase> getPhrases() {
        return Lists.immutable.withAll(phrases);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonSentence that)) {
            return false;
        }
        return sentenceNumber == that.sentenceNumber && startIndexInclude == that.startIndexInclude && endIndexInclude == that.endIndexInclude
                && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceNumber, text, startIndexInclude, endIndexInclude);
    }
}
