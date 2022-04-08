/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;

public class JsonText implements IText, Serializable {
    @JsonProperty
    private List<JsonWord> words = new ArrayList<>();
    @JsonProperty
    private List<JsonSentence> sentences = new ArrayList<>();
    @JsonProperty
    private List<JsonCorefClusters> corefClusters = new ArrayList<>();

    public JsonText() {
        // NOP
    }

    public JsonText(IText text) {
        words = text.getWords().collect(w -> new JsonWord(text, this, w)).stream().toList();
        sentences = text.getSentences().collect(s -> new JsonSentence(text, this, s)).stream().toList();
        corefClusters = text.getCorefClusters().collect(c -> new JsonCorefClusters(text, this, c)).stream().toList();
    }

    public void init() {
        words.forEach(w -> w.init(this));
        sentences.forEach(js -> js.init(this));
        corefClusters.forEach(cc -> cc.init(this));
    }

    @Override
    public IWord getFirstWord() {
        return words.stream().findFirst().orElse(null);
    }

    @Override
    public ImmutableList<IWord> getWords() {
        return Lists.immutable.withAll(words);
    }

    @Override
    public ImmutableList<ICorefCluster> getCorefClusters() {
        return Lists.immutable.withAll(corefClusters);
    }

    @Override
    public ImmutableList<ISentence> getSentences() {
        return Lists.immutable.withAll(sentences);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JsonText jsonText = (JsonText) o;
        return Objects.equals(words, jsonText.words) && Objects.equals(sentences, jsonText.sentences) && Objects.equals(corefClusters, jsonText.corefClusters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(words, sentences, corefClusters);
    }
}
