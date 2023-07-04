/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import io.github.ardoco.textproviderjson.dto.TextDTO;

/**
 * utility class to convert a text DTO into json and back
 **/
public final class JsonConverter {

    private static final String SCHEMA_PATH = "schemas/text.json";

    private JsonConverter() {

    }

    /***
     * checks whether the json string matches the text schema
     * 
     * @param json the json string
     * @return whether the json string matches the text schema
     */
    public static boolean validateJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

        InputStream inputSchema = JsonConverter.class.getClassLoader().getResourceAsStream(SCHEMA_PATH);
        JsonSchema schema = schemaFactory.getSchema(inputSchema);

        Set<ValidationMessage> message = schema.validate(mapper.readTree(json));
        return message.isEmpty();
    }

    /**
     * generates the corresponding text DTO of the json string
     * 
     * @param json the json string
     * @return the corresponding text DTO
     */
    public static TextDTO fromJsonString(String json) throws IOException {
        if (validateJson(json)) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, TextDTO.class);
        }
        return null;
    }

    /**
     * converts the text DTO into json string. Returns null if the JSON could not be validated.
     * 
     * @param obj the text DTO
     * @return the JSON string or null
     */
    public static String toJsonString(TextDTO obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(obj);
        if (!validateJson(jsonString)) {
            return null;
        }
        return jsonString;
    }
}
