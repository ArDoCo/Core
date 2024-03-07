/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.BoxGSTest;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.DiagramGoldStandardTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.DiagramTextTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.DiagramWordTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

class DiagramTextTraceLinkTest {
    public static final Word mockWord = Mockito.mock(Word.class, Mockito.withSettings().serializable());
    public static final Sentence mockSentence = Mockito.mock(Sentence.class, Mockito.withSettings().serializable());
    public static final Phrase mockPhrase = Mockito.mock(Phrase.class, Mockito.withSettings().serializable());

    static {
        Mockito.doReturn("The SomeText example").when(mockPhrase).getText();
        Mockito.doReturn("SomeText").when(mockWord).getText();
        Mockito.doReturn(0).when(mockWord).getPosition();
        Mockito.doReturn(0).when(mockWord).getSentenceNo();
        Mockito.doReturn(mockPhrase).when(mockWord).getPhrase();
        Mockito.doReturn(mockSentence).when(mockWord).getSentence();
        Mockito.doReturn("The SomeText example is part of a sentence").when(mockSentence).getText();
        Mockito.doReturn(0).when(mockSentence).getSentenceNumber();
        Mockito.doReturn(1).when(mockSentence).getSentenceNumberForOutput();
    }

    public static final DiagramTextTraceLink DUMMY_DIAGRAM_TEXT_TRACE_LINK_SENTENCE = new DiagramGoldStandardTraceLink(BoxGSTest.DUMMY_BOX_GS, mockSentence,
            "SomeIdentifier", "SomeStandard.json");
    public static final DiagramTextTraceLink DUMMY_DIAGRAM_TEXT_TRACE_LINK_WORD = new DiagramWordTraceLink(BoxGSTest.DUMMY_BOX_GS, mockWord, "SomeIdentifier",
            0.5, null);

    public static List<DiagramTextTraceLink> getDummyDiaTexTraceLinks() {
        return List.of(DUMMY_DIAGRAM_TEXT_TRACE_LINK_SENTENCE, DUMMY_DIAGRAM_TEXT_TRACE_LINK_WORD);
    }

    @DisplayName("Evaluate Serialize DiaTextTraceLinks")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDummyDiaTexTraceLinks")
    void serialize(DiagramTextTraceLink diagramTextTraceLink) throws IOException, ClassNotFoundException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(byteArrayOutputStream).writeObject(diagramTextTraceLink);
        var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        var deserialized = (DiagramTextTraceLink) new ObjectInputStream(byteArrayInputStream).readObject();

        assertEquals(diagramTextTraceLink, deserialized);
    }

    @DisplayName("Evaluate Serialize DiaTextTraceLinks")
    @Test
    void serialize() throws IOException, ClassNotFoundException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(byteArrayOutputStream).writeObject(Sets.immutable.fromStream(getDummyDiaTexTraceLinks().stream()));
        var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        var deserialized = (ImmutableSet<DiagramTextTraceLink>) new ObjectInputStream(byteArrayInputStream).readObject();

        assertTrue(getDummyDiaTexTraceLinks().containsAll(deserialized.toList()));
        assertTrue(deserialized.containsAll(getDummyDiaTexTraceLinks()));
    }
}
