/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;

public final class JsonTextProvider implements ITextConnector, Serializable {

    @Serial
    private static final long serialVersionUID = 1888754797397675739L;

    @JsonProperty
    private final Map<String, JsonText> texts = new HashMap<>();

    public IText addNewText(String name, IText text) {
        var jText = new JsonText(text);
        texts.put(name, jText);
        return jText;
    }

    public void removeExistingTexts() {
        this.texts.clear();
    }

    @Override
    public IText getAnnotatedText() {
        if (texts.isEmpty()) {
            return null;
        }
        if (texts.size() > 1) {
            throw new IllegalStateException("Multiple texts are defined. Use this::getAnnotatedText(String)");
        }
        return texts.values().iterator().next();
    }

    @Override
    public IText getAnnotatedText(String textName) {
        return texts.get(textName);
    }

    public static JsonTextProvider loadFromFile(File f) throws IOException {
        var texts = getMapper().readValue(f, JsonTextProvider.class);
        texts.texts.values().forEach(JsonText::init);
        return texts;
    }

    public void saveTexts(File destination) throws IOException {
        getMapper().writeValue(destination, this);
    }

    private static ObjectMapper getMapper() {
        var oom = new ObjectMapper();
        oom.setVisibility(oom.getSerializationConfig()
                .getDefaultVisibilityChecker() //
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)//
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)//
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)//
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        return oom;
    }

}
