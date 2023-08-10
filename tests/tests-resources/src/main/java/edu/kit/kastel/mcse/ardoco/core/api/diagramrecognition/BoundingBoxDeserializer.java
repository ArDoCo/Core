package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class BoundingBoxDeserializer extends StdDeserializer<BoundingBox> {
    public BoundingBoxDeserializer() {
        this(BoundingBox.class);
    }

    protected BoundingBoxDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public BoundingBox deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        var x = node.get("x").asInt();
        var y = node.get("y").asInt();
        var w = node.get("w").asInt();
        var h = node.get("h").asInt();

        return new BoundingBox(x, y, x + w, y + h);
    }
}
