/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;

public class JsonCorefClusters implements ICorefCluster, Serializable {

    @Serial
    private static final long serialVersionUID = 3402564508378184208L;

    @JsonProperty
    private int id;
    @JsonProperty
    private String representativeMention;
    @JsonProperty
    private List<List<Integer>> words;
    private transient JsonText parent;

    public JsonCorefClusters() {
        // NOP
    }

    public JsonCorefClusters(IText source, JsonText parent, ICorefCluster cluster) {
        this.parent = parent;
        this.id = cluster.id();
        this.representativeMention = cluster.representativeMention();
        var textWords = source.getWords();
        this.words = cluster.mentions().collect(list -> list.collect(textWords::indexOf).stream().toList()).stream().toList();
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String representativeMention() {
        return representativeMention;
    }

    @Override
    public ImmutableList<ImmutableList<IWord>> mentions() {
        var textWords = parent.getWords();
        return Lists.immutable.withAll(words).collect(list -> Lists.immutable.withAll(list).collect(textWords::get));
    }

    public void init(JsonText parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (JsonCorefClusters) o;
        return id == that.id && Objects.equals(representativeMention, that.representativeMention) && Objects.equals(words, that.words);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, representativeMention, words);
    }
}
