package io.github.ardoco.textproviderjson;

import io.github.ardoco.textproviderjson.converter.JsonConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class ValidatorTest {

    @Test
    void testValidating () throws IOException {
        Assertions.assertTrue(JsonConverter.validateJson(Files.readString(Path.of("./src/test/resources/valid-example-text.json"))));
        Assertions.assertFalse(JsonConverter.validateJson(Files.readString(Path.of("./src/test/resources/invalid-example-text.json"))));

    }

}
