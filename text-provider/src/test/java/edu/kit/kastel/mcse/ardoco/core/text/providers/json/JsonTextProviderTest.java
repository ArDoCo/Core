/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.File;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;

@TestMethodOrder(OrderAnnotation.class)
class JsonTextProviderTest {
    protected static String path = "src/test/resources/teastore.json";

    private JsonTextProvider textProvider;

    @BeforeEach
    void beforeEach() throws Exception {
        textProvider = JsonTextProvider.loadFromFile(new File(path));
    }

    @AfterEach
    void afterEach() {
        textProvider = null;
    }

    /**
     * Repeated test to be able to look at the performance a little bit better. First run is usually slower as parts of
     * the ontology still need to be loaded/cached. Subsequent runs are generally faster
     */
    @Order(1)
    @RepeatedTest(3)
    @DisplayName("Test retrieval of text")
    void getAnnotatedTextTest() {
        var text = textProvider.getAnnotatedText();
        Assertions.assertNotNull(text);
    }

    @Order(2)
    @Test
    void addTextTest() {
        IText text = textProvider.getAnnotatedText();
        Assertions.assertNotNull(text);

        textProvider.addNewText("test", text);
        IText newText = textProvider.getAnnotatedText("test");
        Assertions.assertEquals(text.getLength(), newText.getLength());
        Assertions.assertEquals(text.getFirstWord(), newText.getFirstWord());
    }

    @Order(3)
    @Test
    void removeExistingTextsTest() {
        textProvider.removeExistingTexts();
        var text = textProvider.getAnnotatedText();
        Assertions.assertNull(text);
    }

}
