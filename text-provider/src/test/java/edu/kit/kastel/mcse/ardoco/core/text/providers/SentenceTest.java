/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers;

import java.io.FileInputStream;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.CoreNLPProvider;

/**
 *
 */
class SentenceTest {
    private static final String textPath = "src/test/resources/teastore.txt";

    private static ImmutableList<Sentence> sentences;

    @BeforeAll
    static void beforeAll() throws Exception {
        var connector = new CoreNLPProvider(new DataRepository(), new FileInputStream(textPath));
        var ontologyText = connector.getAnnotatedText();
        sentences = ontologyText.getSentences();
    }

    @Test
    void getSentencesTest() {
        Assertions.assertEquals(43, sentences.size());
    }

    @Test
    void getSentenceNumberTest() {
        int expectedSentenceNumber = 0;
        for (var sentence : sentences) {
            Assertions.assertEquals(expectedSentenceNumber, sentence.getSentenceNumber());
            expectedSentenceNumber++;
        }
    }

    @Test
    void getWordsTest() {
        var sentence = sentences.get(0);
        var words = sentence.getWords();
        Assertions.assertEquals(13, words.size());
    }

    @Test
    void getTextTest() {
        var sentence = sentences.get(0);
        var expectedSentenceText = "The TeaStore consists of 5 replicatable services and a single Registry instance.";
        var actualSentenceText = sentence.getText();
        Assertions.assertEquals(expectedSentenceText, actualSentenceText);

        sentence = sentences.get(15);
        expectedSentenceText = "To speed up image delivery, an in-memory cache with Least Frequently Used (LFU) replacemenent strategy is in place.";
        actualSentenceText = sentence.getText();
        Assertions.assertEquals(expectedSentenceText, actualSentenceText);

        sentence = sentences.get(28);
        expectedSentenceText = "Recommendations are generated based on the users current shopping cart, the user's previous orders and/or the item the user is currently looking at.";
        actualSentenceText = sentence.getText();
        Assertions.assertEquals(expectedSentenceText, actualSentenceText);

        sentence = sentences.get(33);
        expectedSentenceText = "One CPU-intensive, calculating the item-rankings per user on-the-go and one memory-intensive, calculating the total user rating prediction matrix during the training phase.";
        actualSentenceText = sentence.getText();
        Assertions.assertEquals(expectedSentenceText, actualSentenceText);

        sentence = sentences.get(42);
        expectedSentenceText = "By limiting it to a single registry instance, it enables easy configuration of multiple parallel TeaStores with minimal configuration overhead.";
        actualSentenceText = sentence.getText();
        Assertions.assertEquals(expectedSentenceText, actualSentenceText);

    }

}
