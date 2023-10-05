package edu.kit.kastel.mcse.ardoco.core.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.jetbrains.annotations.NotNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class represents an abbreviation with a known set of meanings. An abbreviation is a string such as "ArDoCo" and has the meaning "Architecture
 * Documentation Consistency". The abbreviation that is disambiguated by the meanings of this class is final, but the meanings can be changed. An instance of
 * this class can be serialized and deserialized into JSON using Jackson.
 */
@JsonSerialize(using = Disambiguation.DisambiguationSerializer.class)
public class Disambiguation implements Comparable<Disambiguation>, Serializable {
    private final String abbreviation;
    private final LinkedHashSet<String> meanings;

    public @NotNull String getAbbreviation() {
        return abbreviation;
    }

    public @NotNull Set<String> getMeanings() {
        return Collections.unmodifiableSet(meanings);
    }

    @Override
    public int compareTo(@NotNull Disambiguation o) {
        return abbreviation.compareTo(o.abbreviation);
    }

    /**
     * Used by the Jackson library to serialize a disambiguation into JSON format.
     */
    public static class DisambiguationSerializer extends JsonSerializer<Disambiguation> {
        @Override
        public void serialize(Disambiguation abbreviation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("abbreviation", abbreviation.abbreviation);
            jsonGenerator.writeArrayFieldStart("meanings");
            var meanings = abbreviation.meanings;
            for (var meaning : meanings) {
                jsonGenerator.writeString(meaning);
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
    }

    /**
     * Creates a new disambiguation of the provided abbreviation using the given array of meanings.
     *
     * @param abbreviation the abbreviation that is disambiguated by this instance
     * @param meanings     an array of meanings for the abbreviation, may be empty
     */
    @JsonCreator
    public Disambiguation(@JsonProperty("abbreviation") @NotNull String abbreviation, @JsonProperty("meanings") @NotNull String[] meanings) {
        this(abbreviation, new LinkedHashSet<>(Arrays.stream(meanings).toList()));
    }

    /**
     * Creates a new disambiguation of the provided abbreviation using the given set of meanings.
     *
     * @param abbreviation the abbreviation that is disambiguated by this instance
     * @param meanings     a set of meanings for the abbreviation, may be empty
     */
    public Disambiguation(@NotNull String abbreviation, @NotNull LinkedHashSet<String> meanings) {
        this.abbreviation = abbreviation;
        this.meanings = meanings;
    }

    /**
     * Adds all meanings from another disambiguation to this disambiguation. Be careful, this does not perform any checks regarding the abbreviations.
     *
     * @param other the other disambiguation
     * @return this
     */
    public @NotNull Disambiguation addMeanings(Disambiguation other) {
        meanings.addAll(other.meanings);
        return this;
    }

    /**
     * Searches the text for meanings contained by this disambiguation and replaces them with the abbreviation.
     *
     * @param text       the text to search
     * @param ignoreCase whether letter case should be ignored when searching for the meanings
     * @return the abbreviated text
     */
    public @NotNull String replaceMeaningWithAbbreviation(@NotNull String text, boolean ignoreCase) {
        var abbreviatedText = text;
        for (String meaning : meanings) {
            String pattern = ignoreCase ? "(?i)" : "";
            pattern += meaning;
            abbreviatedText = abbreviatedText.replaceAll(pattern, abbreviation);
        }
        return abbreviatedText;
    }

    /**
     * Creates a map with entries mapping each abbreviation to its disambiguation. This is useful if a list of disambiguations has to be searched for a
     * particular abbreviation regularly.
     *
     * @param disambiguations a list of disambiguations, may be empty
     * @return an immutable map
     */
    public @NotNull
    static ImmutableMap<String, Disambiguation> toMap(@NotNull List<Disambiguation> disambiguations) {
        var map = Maps.mutable.<String, Disambiguation>empty();
        for (var disambiguation : disambiguations) {
            map.merge(disambiguation.getAbbreviation(), disambiguation, Disambiguation::addMeanings);
        }
        return map.toImmutable();
    }

    /**
     * Merges the first map with the second map in a new map. If a key already exists, the disambiguations are merged non-destructively.
     *
     * @param a the first map
     * @param b the second map
     * @return a mutable map
     */
    @NotNull
    public static MutableMap<String, Disambiguation> merge(@NotNull Map<String, Disambiguation> a, @NotNull Map<String, Disambiguation> b) {
        var mergedMap = Maps.mutable.ofMap(a);
        for (var entry : b.entrySet()) {
            mergedMap.merge(entry.getKey(), entry.getValue(), Disambiguation::merge);
        }
        return mergedMap;
    }

    /**
     * Merges the first disambiguation with the second disambiguation into a new instance.
     *
     * @param a first ambiguation
     * @param b second ambiguation
     * @return new merged disambiguation
     */
    @NotNull
    public static Disambiguation merge(@NotNull Disambiguation a, @NotNull Disambiguation b) {
        var temp = new LinkedHashSet<>(a.meanings);
        temp.addAll(b.meanings);
        return new Disambiguation(a.abbreviation, temp);
    }
}
