/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * This class represents an abbreviation with a known set of meanings. An abbreviation is a string such as "ArDoCo" and has the meaning "Architecture
 * Documentation Consistency". The abbreviation that is disambiguated by the meanings of this class is final, but the meanings can be changed. An instance of
 * this class can be serialized and deserialized into JSON using Jackson.
 */
@Deterministic
@JsonSerialize(using = Disambiguation.DisambiguationSerializer.class)
public class Disambiguation implements Comparable<Disambiguation>, Serializable {
    private final String abbreviation;
    private final SortedSet<String> meanings;

    public String getAbbreviation() {
        return abbreviation;
    }

    public SortedSet<String> getMeanings() {
        return new TreeSet<>(meanings);
    }

    @Override
    public int compareTo(Disambiguation o) {
        return abbreviation.compareTo(o.abbreviation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof Disambiguation other) {
            return getAbbreviation().equals(other.getAbbreviation());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAbbreviation());
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
    public Disambiguation(@JsonProperty("abbreviation") String abbreviation, @JsonProperty("meanings") String[] meanings) {
        this(abbreviation, new TreeSet<>(Arrays.stream(meanings).toList()));
    }

    /**
     * Creates a new disambiguation of the provided abbreviation using the given set of meanings.
     *
     * @param abbreviation the abbreviation that is disambiguated by this instance
     * @param meanings     a set of meanings for the abbreviation, may be empty
     */
    public Disambiguation(String abbreviation, SortedSet<String> meanings) {
        this.abbreviation = abbreviation;
        this.meanings = new TreeSet<>(meanings);
    }

    /**
     * Adds all meanings from another disambiguation to this disambiguation. Be careful, this does not perform any checks regarding the abbreviations.
     *
     * @param other the other disambiguation
     * @return this
     */
    public Disambiguation addMeanings(Disambiguation other) {
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
    public String replaceMeaningWithAbbreviation(String text, boolean ignoreCase) {
        var abbreviatedText = text;
        for (String meaning : meanings) {
            String pattern = ignoreCase ? "(?i)" : "";
            pattern += meaning;
            abbreviatedText = abbreviatedText.replaceAll(pattern, abbreviation);
        }
        return abbreviatedText;
    }

    /**
     * Merges the first map with the second map in a new map. If a key already exists, the disambiguations are merged non-destructively.
     *
     * @param a the first map
     * @param b the second map
     * @return a mutable map
     */

    public static SortedMap<String, Disambiguation> merge(SortedMap<String, Disambiguation> a, SortedMap<String, Disambiguation> b) {
        var mergedMap = new TreeMap<>(a);
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

    public static Disambiguation merge(Disambiguation a, Disambiguation b) {
        var temp = new TreeSet<>(a.meanings);
        temp.addAll(b.meanings);
        return new Disambiguation(a.abbreviation, temp);
    }
}
