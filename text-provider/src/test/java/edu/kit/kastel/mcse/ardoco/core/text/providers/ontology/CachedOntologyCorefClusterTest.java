/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.informalin.ontology.OntologyConnector;

public class CachedOntologyCorefClusterTest extends OntologyCorefClusterTest {

    @Override
    @BeforeEach
    void beforeEach() {
        ontologyConnector = new OntologyConnector(ontologyPath);
        var testClusterIndividual = ontologyConnector.getIndividualByIri(testClusterUri).orElseThrow();
        var tmpOntologyWord = OntologyCorefCluster.get(ontologyConnector, testClusterIndividual, null);
        ontologyCorefCluster = CachedOntologyCorefCluster.get(tmpOntologyWord);
    }

    @Override
    @AfterEach
    void afterEach() {
        ontologyConnector = null;
        ontologyCorefCluster = null;
    }
}
