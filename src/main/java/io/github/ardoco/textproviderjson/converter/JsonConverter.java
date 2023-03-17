package io.github.ardoco.textproviderjson.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.github.ardoco.textproviderjson.dto.TextDTO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * You can deserialize a JSON string with {@code JsonText text = Converter.fromJsonString(jsonString);}
 **/
public class JsonConverter {

    private JsonConverter() {

    }

    public static boolean validateJson(String json) throws IOException {
        // old $schema: "https://json-schema.org/draft/2020-12/schema",
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

        InputStream inputSchema = new FileInputStream("schemas/text.json");
        JsonSchema schema = schemaFactory.getSchema(inputSchema);

        Set<ValidationMessage> message = schema.validate(mapper.readTree(json));
        return message.isEmpty();
    }

    public static TextDTO fromJsonString(String json) throws IOException {
        if (validateJson(json)) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, TextDTO.class);
        }
        return null;
    }

    public static String toJsonString(TextDTO obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(obj);
        if (!validateJson(jsonString)) {
            return null;
        }
        return jsonString;
    }
}
