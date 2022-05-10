/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import com.google.gson.*;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.TraceLink;

import java.lang.reflect.Type;
import java.util.Comparator;

/**
 * Represents a simple trace link by the id of the model and number of the sentence involved.
 */
public record TestLink(String modelId, int sentenceNr) implements Comparable<TestLink> {

    public TestLink(TraceLink traceLink) {
        this(traceLink.getModelElementUid(), traceLink.getSentenceNumber());
    }

    @Override
    public int compareTo(TestLink o) {
        return Comparator.comparing(TestLink::modelId).thenComparing(TestLink::sentenceNr).compare(this, o);
    }

    static class TestLinkSerde implements JsonSerializer<TestLink>, JsonDeserializer<TestLink> {

        @Override public TestLink deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {

            var testLinkStr = jsonElement.getAsString();
            var data = testLinkStr.split("⇔");

            return new TestLink(
                    data[0],
                    Integer.parseInt(data[1])
            );
        }

        @Override public JsonElement serialize(TestLink testLink, Type type, JsonSerializationContext jsonSerializationContext) {
            var testLinkStr = String.format("%s⇔%s", testLink.modelId, testLink.sentenceNr);
            return new JsonPrimitive(testLinkStr);
        }
    }

}
