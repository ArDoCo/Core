/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.api.diagramrecognition;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.BoundingBox;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox;

/**
 * Deserializes a JSON text box node into a {@link TextBox}. For example the JSON node
 * <pre>
 * {@code
 *  [
 *      {
 *          "text": "User Management",
 *          "boundingBox": {
 *              "x": 12,
 *              "y": 30,
 *              "w": 50,
 *              "h": 60
 * }
 * }
 * ]
 * }</pre> is converted to a text box with {@code text = "User Management"} and its bounding box with {@code minX = 12, minY
 * = 30, maxX = 62, maxY = 90}.
 */
public class TextBoxDeserializer extends StdDeserializer<TextBox> {
    public TextBoxDeserializer() {
        this(TextBox.class);
    }

    protected TextBoxDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TextBox deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        var text = node.get("text").asText();
        var boundingBox = deserializationContext.readValue(node.get("boundingBox").traverse(jsonParser.getCodec()), BoundingBox.class);
        return new TextBox(boundingBox.minX(), boundingBox.minY(), boundingBox.width(), boundingBox.height(), 1, text);
    }
}
