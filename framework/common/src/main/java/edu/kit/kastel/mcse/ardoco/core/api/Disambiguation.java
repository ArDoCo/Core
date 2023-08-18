package edu.kit.kastel.mcse.ardoco.core.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = Disambiguation.DisambiguationSerializer.class)
public class Disambiguation implements Serializable {
    private final String abbreviation;
    private final LinkedHashSet<String> meanings;

    public String getAbbreviation() {
        return abbreviation;
    }

    public Set<String> getMeanings() {
        return Collections.unmodifiableSet(meanings);
    }

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

    @JsonCreator
    public Disambiguation(@JsonProperty("abbreviation") @NotNull String abbreviation, @JsonProperty("meanings") @NotNull String[] meanings) {
        this(abbreviation, new LinkedHashSet<>(Arrays.stream(meanings).toList()));
    }

    public Disambiguation(@NotNull String abbreviation, @NotNull LinkedHashSet<String> meanings) {
        this.abbreviation = abbreviation;
        this.meanings = meanings;
    }

    public Disambiguation addMeanings(Disambiguation other) {
        meanings.addAll(other.meanings);
        return this;
    }

    public String replaceMeaningWithAbbreviation(String text, boolean ignoreCase) {
        var abbreviatedText = text;
        for (String meaning : meanings) {
            if (ignoreCase)
                abbreviatedText = abbreviatedText.replace(meaning.toLowerCase(Locale.US), abbreviation);
            abbreviatedText = abbreviatedText.replace(meaning, abbreviation);
        }
        return abbreviatedText;
    }
}
