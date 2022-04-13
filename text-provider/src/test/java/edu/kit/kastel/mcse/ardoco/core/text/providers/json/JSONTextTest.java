/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.File;

import org.junit.jupiter.api.*;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;

class JSONTextTest {
    protected static String path = "src/test/resources/teastore.json";

    protected JsonTextProvider connector;
    protected IText text;

    @BeforeEach
    void beforeEach() throws Exception {
        connector = JsonTextProvider.loadFromFile(new File(path));
        text = connector.getAnnotatedText();
    }

    @AfterEach
    void afterEach() {
        connector = null;
        text = null;
    }

    @Test
    @DisplayName("Test retrieval of words")
    void getWordsTest() {
        var words = text.getWords();
        Assertions.assertNotNull(words);

        var expectedWords = 764;
        Assertions.assertEquals(expectedWords, words.size());

        // test order
        for (int i = 0; i < words.size(); i++) {
            var word = words.get(i);
            var position = word.getPosition();
            Assertions.assertEquals(i, position);
        }
    }

    @Test
    @DisplayName("Test getLength()")
    void getLengthTest() {
        var length = text.getLength();
        var expectedWords = 764;
        Assertions.assertEquals(expectedWords, length);
    }

    @Test
    @DisplayName("Test retrieval of start node")
    void getFirstWordTest() {
        var startNode = text.getFirstWord();
        Assertions.assertNotNull(startNode);

        var startNodeText = startNode.getText();
        var expectedText = "The";
        Assertions.assertEquals(expectedText, startNodeText);
    }

    @Test
    @DisplayName("Test retrieval of CorefClusters")
    void getCorefClustersTest() {
        var clusters = text.getCorefClusters();
        Assertions.assertNotNull(clusters);

        var expectedNumberOfClusters = 16;
        Assertions.assertEquals(expectedNumberOfClusters, clusters.size());
    }

    @Test
    @DisplayName("Test retrieval of sentences")
    void getSentencesTest() {
        var sentences = text.getSentences();
        Assertions.assertEquals(43, sentences.size());
    }
}
