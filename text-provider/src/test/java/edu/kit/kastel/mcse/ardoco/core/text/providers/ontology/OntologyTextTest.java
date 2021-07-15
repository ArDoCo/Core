package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;

@RunWith(JUnitPlatform.class)
class OntologyTextTest {
    private static String ontologyPath = "src/test/resources/teastore_w_text.owl";

    private OntologyConnector ontologyConnector;
    private OntologyText ontologyText;

    @BeforeEach
    void beforeEach() {
        ontologyConnector = new OntologyConnector(ontologyPath);
        ontologyText = OntologyText.get(ontologyConnector);
    }

    @AfterEach
    void afterEach() {
        ontologyConnector = null;
        ontologyText = null;
    }

    @Test
    @DisplayName("Test retrieval of words")
    void getWordsTest() {
        var words = ontologyText.getWords();
        Assertions.assertNotNull(words);

        var expectedWords = 763;
        Assertions.assertEquals(expectedWords, words.size());
    }

    @Test
    @DisplayName("Test getLength()")
    void getLengthTest() {
        var length = ontologyText.getLength();
        var expectedWords = 763;
        Assertions.assertEquals(expectedWords, length);
    }

    @Test
    @DisplayName("Test retrieval of start node")
    void getStartNodeTest() {
        var startNode = ontologyText.getStartNode();
        Assertions.assertNotNull(startNode);

        var startNodeText = startNode.getText();
        var expectedText = "The";
        Assertions.assertEquals(expectedText, startNodeText);
    }
}
