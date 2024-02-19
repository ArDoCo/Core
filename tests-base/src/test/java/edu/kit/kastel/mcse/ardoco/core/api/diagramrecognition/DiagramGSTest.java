/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.DiagramProject;

public class DiagramGSTest {
    public static final DiagramGS DUMMY_DIAGRAM_GS = new DiagramGS(DiagramProject.TEAMMATES, "SomePath.jpg", new BoxGS[] {});

    @Test
    void serialize() throws IOException, ClassNotFoundException {
        var serialize = DUMMY_DIAGRAM_GS;
        var byteArrayOutputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(byteArrayOutputStream).writeObject(serialize);
        var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        var deserialized = (DiagramGS) new ObjectInputStream(byteArrayInputStream).readObject();

        assertEquals(serialize.getResourceName(), deserialized.getResourceName());
        assertTrue(serialize.getBoxes().containsAll(deserialized.getBoxes()));
        assertTrue(deserialized.getBoxes().containsAll(serialize.getBoxes()));
        assertTrue(serialize.getTextBoxes().containsAll(deserialized.getTextBoxes()));
        assertTrue(deserialized.getTextBoxes().containsAll(serialize.getTextBoxes()));
        assertTrue(serialize.getTraceLinks(null).containsAll(deserialized.getTraceLinks(null)));
        assertTrue(deserialized.getTraceLinks(null).containsAll(serialize.getTraceLinks(null)));
        assertEquals(serialize.getDiagramProject(), deserialized.getDiagramProject());
    }
}
