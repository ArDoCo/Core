package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class DiagramGTest {
    public static final DiagramG dummyDiagramG = new DiagramG(DiagramProject.TEAMMATES, "SomePath.jpg", new BoxG[] {});

    @Test
    void serialize() throws IOException, ClassNotFoundException {
        var serialize = dummyDiagramG;
        var byteArrayOutputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(byteArrayOutputStream).writeObject(serialize);
        var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        var deserialized = (DiagramG) new ObjectInputStream(byteArrayInputStream).readObject();

        assertEquals(serialize.getPath(), deserialized.getPath());
        assertTrue(serialize.getBoxes().containsAll(deserialized.getBoxes()));
        assertTrue(deserialized.getBoxes().containsAll(serialize.getBoxes()));
        assertTrue(serialize.getTextBoxes().containsAll(deserialized.getTextBoxes()));
        assertTrue(deserialized.getTextBoxes().containsAll(serialize.getTextBoxes()));
        assertTrue(serialize.getTraceLinks().containsAll(deserialized.getTraceLinks()));
        assertTrue(deserialized.getTraceLinks().containsAll(serialize.getTraceLinks()));
        assertEquals(serialize.getProject(), deserialized.getProject());
    }
}
