package io.github.ardoco.textproviderjson;

import io.github.ardoco.textproviderjson.converter.JsonConverter;
import io.github.ardoco.textproviderjson.dto.JsonText;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonConverterTest {
    private static Logger logger = LoggerFactory.getLogger(JsonConverterTest.class);
    private static Path exampleFilePath;

    @BeforeAll
    static void beforeAll() {
        exampleFilePath = new File("../examples/example-text.json").toPath();
    }

    private static JsonText getJsonText(Path filePath) {
        JsonText text = null;
        try {
            String jsonString = Files.readString(filePath);
            text = JsonConverter.fromJsonString(jsonString);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        return text;
    }

    @Test
    @Order(1)
    void conversionTest() {
        JsonText text = getJsonText(exampleFilePath);
        Assertions.assertNotNull(text);
    }

    @Test
    @Order(2)
    void invalidConversionTest() {
        exampleFilePath = new File("./src/test/resources/invalid-example-text.json").toPath();
        JsonText text = getJsonText(exampleFilePath);
        Assertions.assertNotNull(text);
    }

    @Test
    @Order(3)
    void contentTest() {
        JsonText text = getJsonText(exampleFilePath);
        Assertions.assertEquals(2, text.getSentences().size());
        for (var sentence : text.getSentences()) {
            for (var word : sentence.getWords()) {
                Assertions.assertNotNull(word.getPosTag());
                Assertions.assertNotNull(word.getLemma());
                Assertions.assertTrue(word.getId() > 0);
            }
        }
    }

}
