package edu.kit.kastel.mcse.ardoco.core.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Literal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Tests the {@link OntologyConnector}. As the {@link OntologyConnector} is only a utility class mostly using the Apache
 * Jena library, we won't test the core functionality of Jena but assume it to work properly. We rather test some of the
 * more elaborated methods that need to combine different calls to check if our logic here is correct.
 *
 * @author Jan Keim
 *
 */
@RunWith(JUnitPlatform.class)
class OntologyConnectorTest {
    private static String ontologyPath = "src/test/resources/mediastore.owl";

    private static final String NONEXISTENT = "><$NONEXISTENT!><";
    private static final String LABEL_E_CLASS = "EClass";
    private static final String URI_E_CLASS = "https://informalin.github.io/knowledgebases/informalin_base_ecore.owl#OWLClass_EClass";
    private static final String LOCAL_IRI_NAMED_ELEMENT = "NamedElement";
    private static final String URI_NAMED_ELEMENT = "https://informalin.github.io/knowledgebases/informalin_base_pcm.owl#NamedElement";
    private static final String LABEL_SYSTEM = "defaultSystem";
    private static final String URI_SYSTEM = "https://informalin.github.io/knowledgebases/examples/mediastore.owl#System_oPwBYHDhEeSqnN80MQ2uGw";
    private static final String BASIC_COMPONENT = "BasicComponent";
    private static final String OBJECT_PROPERTY_URI = "https://informalin.github.io/knowledgebases/informalin_base_pcm.owl#basicComponent_ServiceEffectSpecification_-_ServiceEffectSpecification";
    private static final String OBJECT_PROPERTY_LABEL = "basicComponent_ServiceEffectSpecification_-_ServiceEffectSpecification";
    private static final String DATA_PROPERTY_URI = "https://informalin.github.io/knowledgebases/informalin_base_pcm.owl#entityName_-_NamedElement";
    private static final String DATA_PROPERTY_LABEL = "entityName_-_NamedElement";
    private static final String TEST_LIST_LABEL = "TestList";

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
        Assertions.assertEquals(URI_E_CLASS, clazz.get().getURI(), "Found class has invalid URI.");
    }

    @Test
    @DisplayName("Test getClass() with existing and correct URIs")
    void getClassWithUriTest() {
        var clazz = ontologyConnector.getClass(LOCAL_IRI_NAMED_ELEMENT);
        Assertions.assertTrue(clazz.isPresent(), "Could not find class with name in Iri.");
        Assertions.assertEquals(URI_NAMED_ELEMENT, clazz.get().getURI(), "Found class has invalid URI.");
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
    void getIndividualsOfClassTest() {
        var instances = ontologyConnector.getIndividualsOfClass(BASIC_COMPONENT);
        var expectedNumberOfInstances = 14;
        Assertions.assertEquals(expectedNumberOfInstances, instances.size(), "Number of instances for BasicComponent differs");

        instances = ontologyConnector.getIndividualsOfClass("BooleanOperations");
        expectedNumberOfInstances = 3;
        Assertions.assertEquals(expectedNumberOfInstances, instances.size(), "Number of instances for BooleanOperations differs");

    }

    @Test
    @DisplayName("Test if retrieval of single individual via name works")
    void getIndividualTest() {
        var individual = ontologyConnector.getIndividual(LABEL_SYSTEM);
        Assertions.assertTrue(individual.isPresent(), "Could not find expected individual.");
        Assertions.assertEquals(URI_SYSTEM, individual.get().getURI(), "Found individual for \"System\" has invalid URI.");

        // try to get a non-individual and non-existing individuals, which should fail
        individual = ontologyConnector.getIndividual(LABEL_E_CLASS);
        Assertions.assertTrue(individual.isEmpty(), "Unexpectedly found individual that should not be present.");
        individual = ontologyConnector.getIndividual(NONEXISTENT);
        Assertions.assertTrue(individual.isEmpty(), "Unexpectedly found individual that should not be present.");
    }

    @Test
    @DisplayName("Test if retrieval of single individual via uri")
    void getIndividualByUriTest() {
        var individual = ontologyConnector.getIndividualByIri(URI_SYSTEM);
        Assertions.assertTrue(individual.isPresent(), "Could not find expected individual.");
    }

    @Test
    @DisplayName("Test creation of an empty ordered list")
    void createEmptyListTest() {
        var olo = ontologyConnector.addEmptyList("TestEmptyList");
        Assertions.assertNotNull(olo);
        Assertions.assertEquals(0, olo.size(), "Empty list should have size 0!");
    }

    @Test
    @DisplayName("Test creation of an empty ordered list")
    void createMultipleEmptyListTest() {
        var olo = ontologyConnector.addEmptyList("TestEmptyList1");
        Assertions.assertNotNull(olo);
        Assertions.assertEquals(0, olo.size(), "Empty list should have size 0!");
        var olo2 = ontologyConnector.addEmptyList("TestEmptyList2");
        Assertions.assertNotNull(olo2);
        Assertions.assertEquals(0, olo2.size(), "Empty list should have size 0!");
    }

    @Test
    @DisplayName("Test creation of an empty ordered list where then individuals are added")
    void createEmptyListAndAddIndividualsTest() {
        var listLabel = "TestList" + new Random().nextInt(100);
        var olo = ontologyConnector.addEmptyList(listLabel);
        Assertions.assertNotNull(olo);

        List<Individual> individuals = getExampleIndividuals();
        for (var individual : individuals) {
            olo.add(individual);
        }
        Assertions.assertEquals(individuals.size(), olo.size());
        Assertions.assertIterableEquals(individuals, olo, "List individuals are not equal!");
    }

    @Test
    @DisplayName("Test creation of a populated ordered list")
    void createPopulatedListTest() {
        var listLabel = "TestList" + new Random().nextInt(100);
        List<Individual> individuals = getExampleIndividuals();
        var olo = ontologyConnector.addList(listLabel, individuals);

        Assertions.assertNotNull(olo);
        Assertions.assertEquals(individuals.size(), olo.size());
        Assertions.assertIterableEquals(individuals, olo, "List individuals are not equal!");
    }

    private List<Individual> getExampleIndividuals() {
        List<Individual> individuals = new ArrayList<>();
        Individual system = ontologyConnector.getIndividual(LABEL_SYSTEM).get();
        individuals.add(system);
        Individual cache = ontologyConnector.getIndividual("Cache").get();
        individuals.add(cache);
        return individuals;
    }

    @Test
    @DisplayName("Test retrieval of ordered list")
    void getListTest() {
        var oloOpt = ontologyConnector.getList(TEST_LIST_LABEL);
        Assertions.assertTrue(oloOpt.isPresent(), "Could not find list in ontology");

        var olo = oloOpt.get();
        Assertions.assertEquals(3, olo.size());

        var list = olo.toList();
        Assertions.assertEquals(olo.size(), list.size(), "List in ontology and transformed list of individuals have inequal size!");
    }

    @Test
    @DisplayName("Test retrieval of property")
    void getPropertyTest() {
        var property = ontologyConnector.getProperty(OBJECT_PROPERTY_LABEL);
        Assertions.assertTrue(property.isPresent(), "Could not find expected property.");
        Assertions.assertEquals(OBJECT_PROPERTY_URI, property.get().getURI(), "Found property has invalid URI.");

        // test retrieval of property with prefix notation
        String indexUri = "https://informalin.github.io/knowledgebases/external/olo/orderedlistontology.owl#index";
        property = ontologyConnector.getPropertyByIri("olo:index");
        Assertions.assertTrue(property.isPresent(), "Could not find expected property.");
        Assertions.assertEquals("index", property.get().getLocalName(), "Found property has invalid URI.");
        Assertions.assertEquals(indexUri, property.get().getURI(), "Found property has invalid URI.");

        // test retrieval of property with spaces in its label
        property = ontologyConnector.getProperty("has index");
        Assertions.assertTrue(property.isPresent(), "Could not find expected property.");
        Assertions.assertEquals(indexUri, property.get().getURI(), "Found property has invalid URI.");
    }

    @Test
    @DisplayName("Test retrieval of object property")
    void getObjectPropertyTest() {
        var property = ontologyConnector.getObjectProperty(OBJECT_PROPERTY_LABEL);
        Assertions.assertTrue(property.isPresent(), "Could not find expected property.");
        Assertions.assertEquals(OBJECT_PROPERTY_URI, property.get().getURI(), "Found property has invalid URI.");

        // should not return a valid data property if asked for object property
        property = ontologyConnector.getObjectProperty(DATA_PROPERTY_LABEL);
        Assertions.assertTrue(property.isEmpty(), "Unexpectedly found property.");
    }

    @Test
    @DisplayName("Test retrieval of object property")
    void getDataPropertyTest() {
        var property = ontologyConnector.getDataProperty(DATA_PROPERTY_LABEL);
        Assertions.assertTrue(property.isPresent(), "Could not find expected property.");
        Assertions.assertEquals(DATA_PROPERTY_URI, property.get().getURI(), "Found property has invalid URI.");

        // should not return a valid data property if asked for object property
        property = ontologyConnector.getDataProperty(OBJECT_PROPERTY_LABEL);
        Assertions.assertTrue(property.isEmpty(), "Unexpectedly found property.");
    }

    // TODO tests for adding properties
    @Test
    @DisplayName("Test retrieval of object property")
    void getPropertyValueTest() {
        OntProperty property = ontologyConnector.getDataProperty(DATA_PROPERTY_LABEL).orElseThrow();
        var individual = ontologyConnector.getIndividual(LABEL_SYSTEM).orElseThrow();

        // test data property of string
        var valueNode = ontologyConnector.getPropertyValue(individual, property);
        Assertions.assertNotNull(valueNode);
        Assertions.assertTrue(valueNode.canAs(Literal.class));
        var valueLiteral = valueNode.asLiteral();
        var name = valueLiteral.getString();
        Assertions.assertEquals(LABEL_SYSTEM, name);

        // test data property of int
        individual = ontologyConnector.getIndividual("TestSlot1").orElseThrow();
        property = ontologyConnector.getPropertyByIri("olo:index").orElseThrow();
        valueNode = ontologyConnector.getPropertyValue(individual, property);
        Assertions.assertNotNull(valueNode);
        Assertions.assertTrue(valueNode.canAs(Literal.class));
        valueLiteral = valueNode.asLiteral();
        var index = valueLiteral.getInt();
        Assertions.assertEquals(0, index);

        // test object property
        property = ontologyConnector.getPropertyByIri("olo:next").orElseThrow();
        valueNode = ontologyConnector.getPropertyValue(individual, property);
        Assertions.assertNotNull(valueNode);

        Assertions.assertTrue(valueNode.canAs(Individual.class));
        var expected = ontologyConnector.getIndividual("TestSlot2").orElseThrow();
        var actual = valueNode.as(Individual.class);
        Assertions.assertEquals(expected, actual);

    }

}
