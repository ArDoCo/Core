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

    private static final String NONEXISTENT = "><$NONEXISTENT!><";
    private static final String LABEL_E_CLASS = "EClass";
    private static final String URI_E_CLASS = "https://informalin.github.io/knowledgebases/informalin_base_ecore.owl#OWLClass_EClass";
    private static final String LOCAL_IRI_NAMED_ELEMENT = "NamedElement";
    private static final String URI_NAMED_ELEMENT = "https://informalin.github.io/knowledgebases/informalin_base_pcm.owl#NamedElement";
    private static final String LABEL_SYSTEM = "defaultSystem";
    private static final String URI_SYSTEM = "https://informalin.github.io/knowledgebases/examples/mediastore.owl#System_oPwBYHDhEeSqnN80MQ2uGw";
    private static final String BASIC_COMPONENT = "BasicComponent";

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

    @Test
    @DisplayName("Test getClass() with existing and correct labels")
    void getClassWithLabelTest() {
        var clazz = ontologyConnector.getClass(LABEL_E_CLASS);
        Assertions.assertTrue(clazz.isPresent(), "Could not find class with name in label.");
        Assertions.assertTrue(clazz.get().hasURI(URI_E_CLASS), "Found class has invalid URI.");
    }

    @Test
    @DisplayName("Test getClass() with existing and correct URIs")
    void getClassWithUriTest() {
        var clazz = ontologyConnector.getClass(LOCAL_IRI_NAMED_ELEMENT);
        Assertions.assertTrue(clazz.isPresent(), "Could not find class with name in Iri.");
        Assertions.assertTrue(clazz.get().hasURI(URI_NAMED_ELEMENT), "Found class has invalid URI.");
    }

    @Test
    @DisplayName("Test getClass() with non-existing classes.")
    void getNonexistentClassTest() {
        var clazz = ontologyConnector.getClass(NONEXISTENT);
        Assertions.assertTrue(clazz.isEmpty(), "Found a class although it should be non-existent.");
    }

    @Test
    @DisplayName("Test getClass() with details for non-class entities like individuals.")
    void getClassWithNonClassDetailsTest() {
        var clazz = ontologyConnector.getClass(LABEL_SYSTEM);
        Assertions.assertTrue(clazz.isEmpty(), "Found a class although it should be an individual.");
    }

    @Test
    @DisplayName("Test getting class using Iris.")
    void getClassByIriTest() {
        var clazz = ontologyConnector.getClassByIri(URI_E_CLASS);
        Assertions.assertTrue(clazz.isPresent(), "Could not find class with Iri/Uri.");

        clazz = ontologyConnector.getClassByIri(URI_NAMED_ELEMENT);
        Assertions.assertTrue(clazz.isPresent(), "Could not find class with Iri/Uri.");

        clazz = ontologyConnector.getClassByIri(NONEXISTENT);
        Assertions.assertTrue(clazz.isEmpty(), "Found a class although it should be non-existent.");
    }

    @Test
    @DisplayName("Test retrieval of individuals of a certain class")
    void getInstancesOfClassTest() {
        var instances = ontologyConnector.getInstancesOfClass(BASIC_COMPONENT);
        var expectedNumberOfInstances = 14;
        Assertions.assertEquals(expectedNumberOfInstances, instances.size(), "Number of instances for BasicComponent differs");

        instances = ontologyConnector.getInstancesOfClass("BooleanOperations");
        expectedNumberOfInstances = 3;
        Assertions.assertEquals(expectedNumberOfInstances, instances.size(), "Number of instances for BooleanOperations differs");

    }

}
