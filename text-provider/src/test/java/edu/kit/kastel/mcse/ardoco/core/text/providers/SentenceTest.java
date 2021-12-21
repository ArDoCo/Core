/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ontology.CachedOntologyText;

/**
 * @author Jan Keim
 *
 */
class SentenceTest {
    private static String ontologyPath = "src/test/resources/teastore_w_text.owl";

    private static ImmutableList<ISentence> sentences;

    @BeforeAll
    static void beforeAll() {
        var ontologyConnector = new OntologyConnector(ontologyPath);
        var ontologyText = CachedOntologyText.get(ontologyConnector);
        sentences = ontologyText.getSentences();
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
    }

}
