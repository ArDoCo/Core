/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

class CoreNLPSentenceTest {

    protected static String inputText = "src/test/resources/teastore.txt";
    private static ISentence sentence;

    @BeforeAll static void beforeAll() {
        IText text;
        try {
            var coreNLPProvider = new CoreNLPProvider(new FileInputStream(inputText));
            text = coreNLPProvider.getAnnotatedText();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        sentence = text.getSentences().get(41);
    }

    @Test void getTextTest() {
        Assertions.assertEquals("The TeaStore is a test application.", sentence.getText());
    }

    @Test void getWordsTest() {
        var words = sentence.getWords();
        Assertions.assertEquals(7, words.size());
    }

    @Test void getSentenceNoTest() {
        Assertions.assertEquals(41, sentence.getSentenceNumber());
    }
}
