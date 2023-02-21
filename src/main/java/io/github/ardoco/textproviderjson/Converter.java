package io.github.ardoco.textproviderjson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.SchemaException;
import io.vertx.json.schema.SchemaRepository;
import io.vertx.json.schema.Validator;

import java.io.InputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * You can deserialize a JSON string with {@code Textschema text = Converter.fromJsonString(jsonString);}
 **/
public class Converter {


    private static ObjectReader reader;
    private static ObjectWriter writer;

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
        JsonSchemaOptions jsonSchemaOptions = new JsonSchemaOptions().setBaseUri("https://ardoco.github.io/textprovider-json/schemas/");
        SchemaRepository repository = SchemaRepository.create(jsonSchemaOptions);
        var validator = repository.validator("/text.json");
        // TODO hier weitermachen!
        var filePath = new File("../schemas/text.json").toPath();
        String schemaString = Files.readString(filePath);
        JsonSchema schema = JsonSchema.of(new JsonObject(schemaString));
        OutputUnit result = Validator.create(schema, jsonSchemaOptions).validate(json);
        System.out.println(result.getValid());
        System.out.println(result.getError());
        return result.getValid();
    }

    public static JsonText fromJsonString(String json) throws IOException {
        // TODO validate
        if (validateJson(json)) {
            return getObjectReader().readValue(json);
        }
        return null;
    }

    public static String toJsonString(JsonText obj) throws IOException {
        String outputString = getObjectWriter().writeValueAsString(obj);

        // TODO validate
        return outputString;
    }


    private static void instantiateMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
            @Override
            public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                String value = jsonParser.getText();
                return Converter.parseDateTimeString(value);
            }
        });
        mapper.registerModule(module);
        reader = mapper.readerFor(JsonText.class);
        writer = mapper.writerFor(JsonText.class);
    }

    private static ObjectReader getObjectReader() {
        if (reader == null) instantiateMapper();
        return reader;
    }

    private static ObjectWriter getObjectWriter() {
        if (writer == null) instantiateMapper();
        return writer;
    }
}
