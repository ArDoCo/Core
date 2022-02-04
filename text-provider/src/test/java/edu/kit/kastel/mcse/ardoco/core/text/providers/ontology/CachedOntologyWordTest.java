/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.informalin.ontology.OntologyConnector;

public class CachedOntologyWordTest extends OntologyWordTest {

    @Override
    @BeforeEach
    void beforeEach() {
        ontologyConnector = new OntologyConnector(ontologyPath);
        var testWordIndividual = ontologyConnector.getIndividualByIri(testWordUri);
        var tmpOntologyWord = OntologyWord.get(ontologyConnector, testWordIndividual.orElseThrow(), null);
        ontologyWord = CachedOntologyWord.get(tmpOntologyWord);
    }

    @Override
    @AfterEach
    void afterEach() {
        ontologyConnector = null;
        ontologyWord = null;
    }
}
