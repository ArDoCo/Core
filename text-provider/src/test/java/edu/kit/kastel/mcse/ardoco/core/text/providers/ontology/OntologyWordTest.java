/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;


import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.POSTag;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OntologyWordTest {
    protected static String ontologyPath = "src/test/resources/teastore_w_text.owl";

    protected static final String testWordUri = "https://informalin.github.io/knowledgebases/examples/teastore_w_text.owl#agLDUA9hLD";

    protected OntologyConnector ontologyConnector;
    protected IWord ontologyWord;

    @BeforeEach
    void beforeEach() {
        ontologyConnector = new OntologyConnector(ontologyPath);
        var testWordIndividual = ontologyConnector.getIndividualByIri(testWordUri);
        ontologyWord = OntologyWord.get(ontologyConnector, testWordIndividual.orElseThrow());
    }

    @AfterEach
    void afterEach() {
        ontologyConnector = null;
        ontologyWord = null;
    }

    @Test
    @DisplayName("Test retrieval of sentence number")
    void getSentenceNoTest() {
        var expected = 41;
        var actual = ontologyWord.getSentenceNo();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test retrieval of position")
    void getPositionTest() {
        var expected = 738;
        var actual = ontologyWord.getPosition();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test retrieval of POS")
    void getPOSTest() {
        var expected = POSTag.NOUN;
        var actual = ontologyWord.getPosTag();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test retrieval of text")
    void getTextTest() {
        var expected = "test";
        var actual = ontologyWord.getText();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test retrieval of lemma")
    void getLemmaTest() {
        var expected = "test";
        var actual = ontologyWord.getLemma();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test retrieval of next word")
    void getNextTest() {
        var nextWord = ontologyWord.getNextWord();
        var expectedText = "application";
        Assertions.assertEquals(expectedText, nextWord.getText());
        Assertions.assertEquals(739, nextWord.getPosition());
    }

    @Test
    @DisplayName("Test retrieval of previous word")
    void getPreviousTest() {
        var prevWord = ontologyWord.getPreWord();
        var expectedText = "a";
        Assertions.assertEquals(expectedText, prevWord.getText());
        Assertions.assertEquals(737, prevWord.getPosition());
    }

    @Test
    @DisplayName("Test retrieval of incoming dependencies")
    void getIncomingDependenciesTest() {
        // Images (71)
        var targetUri = "https://informalin.github.io/knowledgebases/examples/teastore_w_text.owl#s0tHXOMtwP";

        var testWordTargetIndividual = ontologyConnector.getIndividualByIri(targetUri).orElseThrow();
        ontologyWord = OntologyWord.get(ontologyConnector, testWordTargetIndividual);

        var deps = ontologyWord.getWordsThatAreDependentOnThis(DependencyTag.NSUBJ);
        Assertions.assertEquals(1, deps.size());

        var depText = deps.get(0).getText();
        Assertions.assertEquals("provides", depText);
    }

    @Test
    @DisplayName("Test retrieval of outgoing dependencies")
    void getOutgoingDependenciesTest() {
        // rankings (595)
        var dependencyWordSourceUri = "https://informalin.github.io/knowledgebases/examples/teastore_w_text.owl#lgDome4c5A";
        var testWordSourceIndividual = ontologyConnector.getIndividualByIri(dependencyWordSourceUri).orElseThrow();
        ontologyWord = OntologyWord.get(ontologyConnector, testWordSourceIndividual);

        var deps = ontologyWord.getWordsThatAreDependencyOfThis(DependencyTag.NMOD);
        Assertions.assertEquals(2, deps.size());

        var depTextList = deps.stream().map(d -> d.getText()).collect(Collectors.toList());
        Assertions.assertTrue(depTextList.contains("go"));
        Assertions.assertTrue(depTextList.contains("user"));

    }
}
