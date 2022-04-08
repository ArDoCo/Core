/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.*;

public class JsonWord implements IWord, Serializable {

    @JsonProperty
    private int sentenceNo;
    @JsonProperty
    private String text;
    @JsonProperty
    private POSTag posTag;
    @JsonProperty
    private int position;
    @JsonProperty
    private String lemma;
    @JsonProperty
    private Map<DependencyTag, List<Integer>> wordsThatAreDependencyOfThis = new HashMap<>();
    @JsonProperty
    private Map<DependencyTag, List<Integer>> wordsThatAreDependentOnThis = new HashMap<>();

    private transient JsonText parent;

    public JsonWord() {
        // NOP
    }

    public JsonWord(IText source, JsonText parent, IWord word) {
        this.parent = parent;
        this.sentenceNo = word.getSentenceNo();
        this.text = word.getText();
        this.posTag = word.getPosTag();
        this.position = word.getPosition();
        this.lemma = word.getLemma();
        // TODO RENAME
        for (var type : DependencyTag.values()) {
            var wordsThatAreDependencyOfThisList = word.getWordsThatAreDependencyOfThis(type).collect(w -> source.getWords().indexOf(w)).stream().toList();
            if (!wordsThatAreDependencyOfThisList.isEmpty())
                wordsThatAreDependencyOfThis.put(type, wordsThatAreDependencyOfThisList);

            var wordsThatAreDependentOnThisList = word.getWordsThatAreDependentOnThis(type).collect(w -> source.getWords().indexOf(w)).stream().toList();
            if (!wordsThatAreDependentOnThisList.isEmpty())
                wordsThatAreDependentOnThis.put(type, wordsThatAreDependentOnThisList);
        }
    }

    public void init(JsonText parent) {
        this.parent = parent;
    }

    @Override
    public int getSentenceNo() {
        return sentenceNo;
    }

    @Override
    public ISentence getSentence() {
        return parent.getSentences().get(sentenceNo);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public POSTag getPosTag() {
        return posTag;
    }

    @Override
    public IWord getPreWord() {
        if (position <= 0)
            return null;
        var words = parent.getWords();
        assert words.get(position) == this;
        return words.get(position - 1);
    }

    @Override
    public IWord getNextWord() {
        var words = parent.getWords();
        if (position >= words.size() - 1)
            return null;
        assert parent.getWords().get(position) == this;
        return parent.getWords().get(position + 1);
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String getLemma() {
        return lemma;
    }

    @Override
    public ImmutableList<IWord> getWordsThatAreDependencyOfThis(DependencyTag dependencyTag) {
        var idxs = wordsThatAreDependencyOfThis.get(dependencyTag);
        if (idxs == null)
            return Lists.immutable.empty();

        var words = parent.getWords();
        return Lists.immutable.withAll(idxs).collect(words::get);
    }

    @Override
    public ImmutableList<IWord> getWordsThatAreDependentOnThis(DependencyTag dependencyTag) {
        var idxs = wordsThatAreDependentOnThis.get(dependencyTag);
        if (idxs == null)
            return Lists.immutable.empty();
        var words = parent.getWords();
        return Lists.immutable.withAll(idxs).collect(words::get);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JsonWord jsonWord = (JsonWord) o;
        return sentenceNo == jsonWord.sentenceNo && position == jsonWord.position && Objects.equals(text, jsonWord.text) && posTag == jsonWord.posTag
                && Objects.equals(lemma, jsonWord.lemma) && Objects.equals(wordsThatAreDependencyOfThis, jsonWord.wordsThatAreDependencyOfThis)
                && Objects.equals(wordsThatAreDependentOnThis, jsonWord.wordsThatAreDependentOnThis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceNo, text, posTag, position, lemma, wordsThatAreDependencyOfThis, wordsThatAreDependentOnThis);
    }
}
