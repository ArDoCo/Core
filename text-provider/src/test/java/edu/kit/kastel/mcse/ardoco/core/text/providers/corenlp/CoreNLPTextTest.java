/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;

class CoreNLPTextTest {
    private static final Logger logger = LoggerFactory.getLogger(CoreNLPTextTest.class);
    protected static String inputText = "src/test/resources/teastore.txt";

    private static IText text;

    @BeforeAll
    static void beforeAll() {
        try {
            var coreNLPProvider = new CoreNLPProvider(new FileInputStream(inputText));
            text = coreNLPProvider.getAnnotatedText();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getWordsTest() {
        var words = text.getWords();
        IWord word = words.get(739);

        Assertions.assertAll(//
                () -> Assertions.assertEquals("test", word.getText()), //
                () -> Assertions.assertEquals(POSTag.NOUN, word.getPosTag()), //
                () -> Assertions.assertEquals("test", word.getLemma()), //
                () -> Assertions.assertEquals(41, word.getSentenceNo()));
        // TODO test dependencies
        var deps = word.getIncomingDependencyWordsWithType(DependencyTag.COMPOUND);
        System.out.println(deps.size());
    }

    @Test
    void getSentencesTest() {
        var sentences = text.getSentences();
        var sentence = sentences.get(41);
        var words = sentence.getWords();

        Assertions.assertAll(//
                () -> Assertions.assertEquals("The TeaStore is a test application.", sentence.getText()), //
                () -> Assertions.assertEquals(7, words.size()), //
                () -> Assertions.assertEquals(text.getWords().get(739), words.get(4)));
    }
}
