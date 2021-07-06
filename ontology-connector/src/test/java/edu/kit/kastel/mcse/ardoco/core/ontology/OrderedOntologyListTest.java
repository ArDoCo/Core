package edu.kit.kastel.mcse.ardoco.core.ontology;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.Individual;
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
public class OrderedOntologyListTest {
    private static Logger logger = LogManager.getLogger();
    private static String ontologyPath = "src/test/resources/mediastore.owl";
    private static String testOutputOntologyPath = "src/test/resources/test_mediastore.owl";

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

}
