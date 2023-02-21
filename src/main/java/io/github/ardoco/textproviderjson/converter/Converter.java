package io.github.ardoco.textproviderjson.converter;

import com.fasterxml.jackson.databind.*;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.github.ardoco.textproviderjson.dto.JsonText;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Set;

/**
 * You can deserialize a JSON string with {@code JsonText text = Converter.fromJsonString(jsonString);}
 **/
public class Converter {

    private Converter() {

    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ISO_DATE_TIME)
            .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .appendOptional(DateTimeFormatter.ISO_INSTANT)
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SX"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .toFormatter()
            .withZone(ZoneOffset.UTC);

    public static OffsetDateTime parseDateTimeString(String str) {
        return ZonedDateTime.from(Converter.DATE_TIME_FORMATTER.parse(str)).toOffsetDateTime();
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

    public static JsonText fromJsonString(String json) throws IOException {
        if (validateJson(json)) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, JsonText.class);
        }
        return null;
    }

    public static String toJsonString(JsonText obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(obj);
        if (!validateJson(jsonString)) {
            return null;
        }
        return jsonString;
    }
}
