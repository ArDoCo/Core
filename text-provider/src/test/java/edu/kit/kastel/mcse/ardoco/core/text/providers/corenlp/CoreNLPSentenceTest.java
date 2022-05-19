/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CoreNLPSentenceTest {
    private static Sentence sentence;

    @BeforeAll
    static void beforeAll() {
        var coreNLPProvider = CoreNLPProviderTest.getCoreNLPProvider();
        var text = coreNLPProvider.getAnnotatedText();
        var textSentence = text.getSentences().get(41);
        if (textSentence instanceof Sentence castSentence) {
            sentence = castSentence;
        } else {
            throw new IllegalStateException("Sentence from CoreNLPProvider has wrong type");
        }
    }

    @Test
    void getTextTest() {
        Assertions.assertEquals("The TeaStore is a test application.", sentence.getText());
    }

    @Test
    void getWordsTest() {
        var words = sentence.getWords();
        Assertions.assertEquals(7, words.size());
    }

    @Test
    void getSentenceNoTest() {
        Assertions.assertEquals(41, sentence.getSentenceNumber());
    }

    @Test
    void getPhrasesTest() {
        var phrases = sentence.getPhrases();
        Assertions.assertEquals(5, phrases.size());
    }
}
