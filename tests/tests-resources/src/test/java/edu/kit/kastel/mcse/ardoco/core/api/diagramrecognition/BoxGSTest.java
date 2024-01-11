/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BoxGSTest {
    public static final BoxGS DUMMY_BOX_GS = new BoxGS(DiagramGSTest.DUMMY_DIAGRAM_GS, DummyObjects.DUMMY_BOUNDING_BOX, new TextBox[] {
            DummyObjects.DUMMY_TEXT_BOX }, new BoxGS[] {}, new TraceLinkGS[] { DummyObjects.DUMMY_TRACE_LINK_GS });

    @DisplayName("Evaluate Serialize BoxG")
    @Test
    void serialize() throws IOException, ClassNotFoundException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(byteArrayOutputStream).writeObject(DUMMY_BOX_GS);
        var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        var deserialized = (BoxGS) new ObjectInputStream(byteArrayInputStream).readObject();

        assertEquals(DUMMY_BOX_GS, deserialized);
    }
}
