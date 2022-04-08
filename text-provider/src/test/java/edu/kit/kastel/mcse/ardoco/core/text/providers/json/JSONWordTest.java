/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.File;

import org.junit.jupiter.api.*;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;

class JSONWordTest {
    protected static String path = "src/test/resources/teastore.json";

    protected JsonTextProvider connector;
    protected IWord word;

    @BeforeEach
    void beforeEach() throws Exception {
        connector = JsonTextProvider.loadFromFile(new File(path));
        var text = connector.getAnnotatedText();
        word = text.getWords().select(w -> w.getText().equals("test")).stream().findFirst().orElseThrow();
    }

    @AfterEach
    void afterEach() {
        connector = null;
        word = null;
    }

    @Test
    @DisplayName("Test retrieval of sentence number")
    void getSentenceNoTest() {
        var expected = 41;
        var actual = word.getSentenceNo();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test retrieval of position")
    void getPositionTest() {
        var expected = 739;
        var actual = word.getPosition();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test retrieval of POS")
    void getPOSTest() {
        var expected = POSTag.NOUN;
        var actual = word.getPosTag();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test retrieval of text")
    void getTextTest() {
        var expected = "test";
        var actual = word.getText();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test retrieval of lemma")
    void getLemmaTest() {
        var expected = "test";
        var actual = word.getLemma();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test retrieval of next word")
    void getNextTest() {
        var nextWord = word.getNextWord();
        var expectedText = "application";
        Assertions.assertEquals(expectedText, nextWord.getText());
        Assertions.assertEquals(740, nextWord.getPosition());
    }

    @Test
    @DisplayName("Test retrieval of previous word")
    void getPreviousTest() {
        var prevWord = word.getPreWord();
        var expectedText = "a";
        Assertions.assertEquals(expectedText, prevWord.getText());
        Assertions.assertEquals(738, prevWord.getPosition());
    }

    @Test
    @DisplayName("Test retrieval of incoming dependencies")
    void getIncomingDependenciesTest() {
        // Images (71)
        word = connector.getAnnotatedText().getWords().get(72);

        var deps = word.getWordsThatAreDependentOnThis(DependencyTag.NSUBJ);
        Assertions.assertEquals(1, deps.size());

        var depText = deps.get(0).getText();
        Assertions.assertEquals("provides", depText);
    }

    @Test
    @DisplayName("Test retrieval of outgoing dependencies")
    void getOutgoingDependenciesTest() {
        // rankings (595)
        word = connector.getAnnotatedText().getWords().get(596);

        var deps = word.getWordsThatAreDependencyOfThis(DependencyTag.NMOD);
        Assertions.assertEquals(2, deps.size());

        var depTextList = deps.stream().map(IWord::getText).toList();
        Assertions.assertTrue(depTextList.contains("go"));
        Assertions.assertTrue(depTextList.contains("user"));

    }
}
