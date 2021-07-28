package edu.kit.kastel.mcse.ardoco.core.ontology;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.Individual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class OrderedOntologyListTest {
    private static String ontologyPath = "src/test/resources/mediastore.owl";

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

    private OrderedOntologyList getTestList() {
        var oloOpt = ontologyConnector.getList(TEST_LIST_LABEL);
        Assertions.assertTrue(oloOpt.isPresent(), "Could not find list in ontology");

        return oloOpt.get();
    }

    private List<Individual> getExampleIndividuals() {
        List<Individual> individuals = new ArrayList<>();
        Individual system = ontologyConnector.getIndividual("defaultSystem").get();
        individuals.add(system);
        Individual cache = ontologyConnector.getIndividual("Cache").get();
        individuals.add(cache);
        return individuals;
    }

    @Test
    @DisplayName("Test retrieval of ordered list")
    void getListTest() {
        var olo = getTestList();
        Assertions.assertEquals(3, olo.size());

        var list = olo.toList();
        Assertions.assertEquals(olo.size(), list.size(), "List in ontology and transformed list of individuals have inequal size!");
    }

    @Test
    @DisplayName("Test adding elements to list")
    void addTest() {
        var olo = getTestList();
        int expectedSize = 3;
        Assertions.assertEquals(expectedSize, olo.size());

        var individuals = getExampleIndividuals();
        for (var individual : individuals) {
            olo.add(individual);
            expectedSize++;
            Assertions.assertEquals(expectedSize, olo.size());
        }

        var oloList = olo.toList();
        for (var individual : individuals) {
            Assertions.assertTrue(oloList.contains(individual));
        }
    }

    @Test
    @DisplayName("Test adding elements to list using index")
    void addIndexedTest() {
        var olo = getTestList();
        int expectedSize = 3;
        Assertions.assertEquals(expectedSize, olo.size());

        var individuals = getExampleIndividuals();
        var i = 0;
        for (var individual : individuals) {
            olo.add(i, individual);
            i += 2;
            Assertions.assertEquals(++expectedSize, olo.size());
        }

        var oloList = olo.toList();
        for (var j = 0; j < individuals.size(); j += 2) {
            Assertions.assertEquals(individuals.get(j / 2), oloList.get(j));
        }
    }

    @Test
    @DisplayName("Test adding collection of elements to list")
    void addAllTest() {
        var olo = getTestList();
        int expectedSize = 3;
        Assertions.assertEquals(expectedSize, olo.size());

        var individuals = getExampleIndividuals();
        olo.addAll(individuals);

        expectedSize += individuals.size();
        Assertions.assertEquals(expectedSize, olo.size());

        var oloList = olo.toList();
        Assertions.assertEquals(expectedSize, oloList.size());

        for (var individual : individuals) {
            Assertions.assertTrue(oloList.contains(individual));
        }
    }

    @Test
    @DisplayName("Test adding collection of elements to a previously empty list")
    void addAllToEmptyListTest() {
        var olo = ontologyConnector.addEmptyList(TEST_LIST_LABEL + 2);
        var individuals = getExampleIndividuals();

        olo.addAll(individuals);
        Assertions.assertEquals(individuals.size(), olo.size());

        var oloList = olo.toList();
        Assertions.assertEquals(individuals.size(), oloList.size());

        for (var individual : individuals) {
            Assertions.assertTrue(oloList.contains(individual));
        }
    }

    @Test
    @DisplayName("Test indexed adding of a collection of elements to list")
    void addAllIndexedTest() {
        var olo = getTestList();
        int expectedSize = 3;
        Assertions.assertEquals(expectedSize, olo.size());

        var individuals = getExampleIndividuals();
        olo.addAll(1, individuals);

        expectedSize += individuals.size();
        Assertions.assertEquals(expectedSize, olo.size());

        var oloList = olo.toList();
        Assertions.assertEquals(expectedSize, oloList.size());

        for (var individual : individuals) {
            Assertions.assertTrue(oloList.contains(individual));
        }
    }

    @Test
    @DisplayName("Test indexed adding of a collection of elements to previously empty list")
    void addAllIndexedToEmptyListTest() {
        var olo = ontologyConnector.addEmptyList(TEST_LIST_LABEL + 2);
        var individuals = getExampleIndividuals();

        olo.addAll(0, individuals);
        Assertions.assertEquals(individuals.size(), olo.size());

        var oloList = olo.toList();
        Assertions.assertEquals(individuals.size(), oloList.size());

        for (var individual : individuals) {
            Assertions.assertTrue(oloList.contains(individual));
        }
    }

    @Test
    @DisplayName("Test retrieval of elements from ordered list")
    void getTest() {
        var olo = getTestList();
        Assertions.assertEquals(3, olo.size());

        Individual userdbadapter = ontologyConnector.getIndividual("UserDBAdapter").get();
        Assertions.assertEquals(userdbadapter, olo.get(1));

        var list = olo.toList();
        Assertions.assertEquals(list.get(2), olo.get(2));
    }

    @Test
    @DisplayName("Test contain method")
    void containsTest() {
        var olo = getTestList();

        Individual userdbadapter = ontologyConnector.getIndividual("UserDBAdapter").get();
        Assertions.assertTrue(olo.contains(userdbadapter));

        Individual facade = ontologyConnector.getIndividual("Facade").get();
        Assertions.assertFalse(olo.contains(facade));
    }

    @Test
    @DisplayName("Test clear method")
    void clearTest() {
        var olo = getTestList();
        olo.clear();

        Assertions.assertEquals(0, olo.size());
        Assertions.assertTrue(olo.isEmpty());

        var oloList = olo.toList();
        Assertions.assertTrue(oloList.isEmpty());
    }

    @Test
    @DisplayName("Test indexOf method")
    void indexOfTest() {
        var olo = getTestList();

        Individual userdbadapter = ontologyConnector.getIndividual("UserDBAdapter").get();
        Assertions.assertEquals(1, olo.indexOf(userdbadapter));

        Individual facade = ontologyConnector.getIndividual("Facade").get();
        Assertions.assertEquals(-1, olo.indexOf(facade));
    }

    @Test
    @DisplayName("Test lastIndexOf method")
    void lastIndexOfTest() {
        var olo = getTestList();

        Individual userdbadapter = ontologyConnector.getIndividual("UserDBAdapter").get();
        Assertions.assertEquals(1, olo.lastIndexOf(userdbadapter));

        Individual facade = ontologyConnector.getIndividual("Facade").get();
        Assertions.assertEquals(-1, olo.lastIndexOf(facade));

        var currSize = olo.size();
        olo.add(userdbadapter);
        Assertions.assertEquals(currSize, olo.lastIndexOf(userdbadapter));
    }

    @Test
    @DisplayName("Test remove method")
    void removeTest() {
        var olo = getTestList();

        Individual userdbadapter = ontologyConnector.getIndividual("UserDBAdapter").get();
        Individual removedIndividual = olo.remove(1);
        Assertions.assertEquals(userdbadapter, removedIndividual);
        Assertions.assertEquals(2, olo.size(), "Size after removal does not match expected size");
    }

}
