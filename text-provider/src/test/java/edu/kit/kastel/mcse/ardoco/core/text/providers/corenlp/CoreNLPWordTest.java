/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

class CoreNLPWordTest {
    public static final int WORD_INDEX = 739;
    protected static String inputText = "src/test/resources/teastore.txt";
    private static IWord word;

    @BeforeAll static void beforeAll() {
        IText text;
        try {
            var coreNLPProvider = new CoreNLPProvider(new FileInputStream(inputText));
            text = coreNLPProvider.getAnnotatedText();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        word = text.getWords().get(WORD_INDEX);
    }

    @Test void getTextTest() {
        Assertions.assertEquals("test", word.getText());
    }

    @Test void getPositionTest() {
        Assertions.assertEquals(WORD_INDEX, word.getPosition());
    }

    @Test void getPOSTagTest() {
        Assertions.assertEquals(POSTag.NOUN, word.getPosTag());
    }

    @Test void getLemmaTest() {
        Assertions.assertEquals("test", word.getLemma());
    }

    @Test void getSentenceNoTest() {
        Assertions.assertEquals(41, word.getSentenceNo());
    }

    @Test void getIncomingDependencyWordsWithTypeTest() {
        var dependencies = word.getIncomingDependencyWordsWithType(DependencyTag.COMPOUND);
        Assertions.assertAll(//
                () -> Assertions.assertEquals(1, dependencies.size()), //
                () -> Assertions.assertEquals("application", dependencies.get(0).getText()));
    }

    @Test void getOutgoingDependencyWordsWithTypeTest() {
        var dependencies = word.getNextWord().getOutgoingDependencyWordsWithType(DependencyTag.COMPOUND);
        Assertions.assertAll(//
                () -> Assertions.assertEquals(1, dependencies.size()), //
                () -> Assertions.assertEquals("test", dependencies.get(0).getText()));
    }

}
