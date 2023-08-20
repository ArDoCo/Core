package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class TextBoxDeserializer extends StdDeserializer<TextBox> {
  public TextBoxDeserializer() {
    this(TextBox.class);
  }

  protected TextBoxDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public TextBox deserialize(JsonParser jsonParser,
                             DeserializationContext deserializationContext) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    var text = node.get("text").asText();
    var boundingBox =
            deserializationContext.readValue(node.get("boundingBox").traverse(jsonParser.getCodec()), BoundingBox.class);
    var textBox = new TextBox(boundingBox.minX(), boundingBox.minY(), boundingBox.width(),
            boundingBox.height(), 1, text);

    //var color = node.get("color").asInt();
    //textBox.setDominatingColor(new Color(color));

    return textBox;
  }
}
