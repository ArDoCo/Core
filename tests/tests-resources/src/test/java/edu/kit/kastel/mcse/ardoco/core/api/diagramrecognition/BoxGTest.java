package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BoxGTest {
    public static final BoxG dummyBoxG = new BoxG(DiagramGTest.dummyDiagramG, BoundingBoxGTest.dummyBoundingBoxG, new TextBoxG[] { TextBoxGTest.dummyTextBoxG },
            new BoxG[] {}, new TraceLinkG[] { TracelinkGTest.DUMMY_TRACE_LINK_G});

    @DisplayName("Evaluate Serialize BoxG")
    @Test
    void serialize() throws IOException, ClassNotFoundException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(byteArrayOutputStream).writeObject(dummyBoxG);
        var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        var deserialized = (BoxG) new ObjectInputStream(byteArrayInputStream).readObject();

        assertEquals(dummyBoxG, deserialized);
    }
}
