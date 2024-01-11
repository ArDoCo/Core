/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializes a JSON bounding box node into a {@link BoundingBox}. For example the JSON node
 * <pre>
 * {@code
 *  {
 *      "x": 12,
 *      "y": 30,
 *      "w": 50,
 *      "h": 60
 * }
 * }</pre> is converted to a bounding box with {@code minX = 12, minY
 * = 30, maxX = 62, maxY = 90}.
 */
public class BoundingBoxDeserializer extends StdDeserializer<BoundingBox> {
    public BoundingBoxDeserializer() {
        this(BoundingBox.class);
    }

    protected BoundingBoxDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public BoundingBox deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        var x = node.get("x").asInt();
        var y = node.get("y").asInt();
        var w = node.get("w").asInt();
        var h = node.get("h").asInt();

        return new BoundingBox(x, y, x + w, y + h);
    }
}
