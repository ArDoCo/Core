package edu.kit.kastel.mcse.ardoco.core.ontology;

import org.apache.jena.ontology.OntModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class OntologyConnectorTest {
    private static Logger logger = LogManager.getLogger();

    private static String ontologyPath = "src/test/resources/mediastore.owl";

    private OntologyConnector ontologyConnector;

    private static OntologyConnector createOntologyConnector(String ontologyPath) {
        return new OntologyConnector(ontologyPath);
    }

    @BeforeEach
    void beforeEach() {
        ontologyConnector = createOntologyConnector(ontologyPath);
    }

    @AfterEach
    void afterEach() {
        ontologyConnector = null;
    }

    @Test
    @DisplayName("Simply load ontology and check if loading was successful")
    void loadOntologyTest() {

        OntModel om = ontologyConnector.getOntModel();
        var ontologies = om.listOntologies().toList();
        Assertions.assertTrue(!ontologies.isEmpty());

        var baseOntologyOpt = ontologyConnector.getBaseOntology();
        Assertions.assertTrue(baseOntologyOpt.isPresent());
        var baseOntology = baseOntologyOpt.get();
        var assumedBaseOntologyUri = "https://informalin.github.io/knowledgebases/examples/mediastore.owl#";
        Assertions.assertEquals(assumedBaseOntologyUri, baseOntology.getURI());
    }

}
