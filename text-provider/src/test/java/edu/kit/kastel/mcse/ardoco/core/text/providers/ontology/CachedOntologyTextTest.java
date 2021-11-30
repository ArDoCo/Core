/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;


import edu.kit.kastel.informalin.ontology.OntologyConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class CachedOntologyTextTest extends OntologyTextTest {

    @Override
    @BeforeEach
    void beforeEach() {
        ontologyConnector = new OntologyConnector(ontologyPath);
        ontologyText = CachedOntologyText.get(ontologyConnector);
    }

    @Override
    @AfterEach
    void afterEach() {
        ontologyConnector = null;
        ontologyText = null;
    }

}
