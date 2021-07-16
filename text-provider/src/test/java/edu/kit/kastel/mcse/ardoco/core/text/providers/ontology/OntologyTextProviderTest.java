package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
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

    @Test
    @DisplayName("Test retrieval of text")
    void getAnnotatedTextTest() {
        var text = ontologyTextProvider.getAnnotatedText();
        Assertions.assertNotNull(text);
    }
}
