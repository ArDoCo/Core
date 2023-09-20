/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.converter.DtoToObjectConverter;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.error.NotConvertableException;

class TextImplTest {
    private static final DtoToObjectConverter CONVERTER = new DtoToObjectConverter();
    private static Text baselineText;
    private Text textImplInstance;

    @BeforeAll
    static void initAll() {
        baselineText = TestUtil.generateTextWithMultipleSentences();
    }

    @BeforeEach
    void init() {
        try {
            textImplInstance = CONVERTER.convertText(TestUtil.generateDTOWithMultipleSentences());
        } catch (NotConvertableException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getLengthTest() {
        Assertions.assertEquals(baselineText.getLength(), textImplInstance.getLength());
    }

    @Test
    void wordsTest() {
        Assertions.assertEquals(baselineText.words().size(), textImplInstance.words().size());
    }

    @Test
    void getWordTest() {
        Assertions.assertEquals(baselineText.getWord(0), textImplInstance.getWord(0));
    }

    @Test
    void getSentencesTest() {
        Assertions.assertEquals(baselineText.getSentences().size(), textImplInstance.getSentences().size());
    }

    @Test
    void simpleHashCodeTest() {
        Assertions.assertEquals(textImplInstance.hashCode(), textImplInstance.hashCode());
    }
}
