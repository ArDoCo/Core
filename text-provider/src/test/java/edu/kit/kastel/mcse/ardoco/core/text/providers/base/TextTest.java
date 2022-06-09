/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.base;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class TextTest {
    private static IText text;

    @BeforeEach
    void beforeEach() {
        var provider = getProvider();
        text = provider.getAnnotatedText();
    }

    protected abstract ITextConnector getProvider();

    @Test
    void getWordsTest() {
        var words = text.getWords();
        IWord word = words.get(739);

        Assertions.assertAll(//
                () -> Assertions.assertEquals("test", word.getText()), //
                () -> Assertions.assertEquals(POSTag.NOUN, word.getPosTag()), //
                () -> Assertions.assertEquals("test", word.getLemma()), //
                () -> Assertions.assertEquals(41, word.getSentenceNo()));
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
