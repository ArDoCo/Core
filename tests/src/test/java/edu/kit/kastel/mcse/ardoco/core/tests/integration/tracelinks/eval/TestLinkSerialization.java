package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import com.google.gson.*;

import java.lang.reflect.Type;

public class TestLinkSerialization implements JsonSerializer<TestLink>, JsonDeserializer<TestLink> {

    @Override
    public TestLink deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var testLinkStr = jsonElement.getAsString();
        var data = testLinkStr.split("⇔");

        return new TestLink(
                data[0],
                Integer.parseInt(data[1])
        );

    }

    @Override
    public JsonElement serialize(TestLink testLink, Type type, JsonSerializationContext jsonSerializationContext) {
        var testLinkStr = String.format("%s⇔%s", testLink.modelId(), testLink.sentenceNr());
        return new JsonPrimitive(testLinkStr);
    }

}
