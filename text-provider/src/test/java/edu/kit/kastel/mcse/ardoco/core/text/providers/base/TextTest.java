/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.TextProvider;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;

public abstract class TextTest {
    private static Text text;

    @BeforeEach
    void beforeEach() {
        var provider = getProvider();
        text = provider.getAnnotatedText();
    }

    protected abstract TextProvider getProvider();

    @Test
    void getWordsTest() {
        var words = text.words();
        Word word = words.get(739);

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
                () -> Assertions.assertEquals(text.words().get(739), words.get(4)));
    }
}
