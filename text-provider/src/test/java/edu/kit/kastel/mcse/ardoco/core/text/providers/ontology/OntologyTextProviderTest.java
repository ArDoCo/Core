/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;

@TestMethodOrder(OrderAnnotation.class)
class OntologyTextProviderTest {
    private static String ontologyPath = "src/test/resources/teastore_w_text.owl";

    private OntologyTextProvider ontologyTextProvider;

    @BeforeEach
    void beforeEach() {
        ontologyTextProvider = OntologyTextProvider.get(ontologyPath);
    }

    @AfterEach
    void afterEach() {
        ontologyTextProvider = null;
    }

    /**
     * Repeated test to be able to look at the performance a little bit better. First run is usually slower as parts of
     * the ontology still need to be loaded/cached. Subsequent runs are generally faster
     */
    @Order(1)
    @RepeatedTest(3)
    @DisplayName("Test retrieval of text")
    void getAnnotatedTextTest() {
        var text = ontologyTextProvider.getAnnotatedText();
        Assertions.assertNotNull(text);
    }

    @Order(2)
    @Test
    void addTextTest() {
        IText text = ontologyTextProvider.getAnnotatedText();
        Assertions.assertNotNull(text);

        ontologyTextProvider.addText(text);
        IText newText = ontologyTextProvider.getAnnotatedText();
        Assertions.assertEquals(text.getLength(), newText.getLength());
        Assertions.assertEquals(text.getFirstWord(), newText.getFirstWord());
    }

    @Order(3)
    @Test
    void removeExistingTextsTest() {
        ontologyTextProvider.removeExistingTexts();
        var text = ontologyTextProvider.getAnnotatedText();
        Assertions.assertNull(text);
    }

}
