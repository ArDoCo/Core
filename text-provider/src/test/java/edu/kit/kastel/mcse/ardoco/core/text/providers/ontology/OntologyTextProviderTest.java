package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

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
    @RepeatedTest(3)
    @DisplayName("Test retrieval of text")
    void getAnnotatedTextTest() {
        var text = ontologyTextProvider.getAnnotatedText();
        Assertions.assertNotNull(text);
    }
}
