/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.text.ICorefCluster;

class OntologyCorefClusterTest {
    protected static String ontologyPath = "src/test/resources/teastore_w_text.owl";

    // Cluster "one single registry"
    protected static final String testClusterUri = "https://informalin.github.io/knowledgebases/examples/teastore_w_text.owl#czdHYODKQq";

    protected OntologyConnector ontologyConnector;
    protected ICorefCluster ontologyCorefCluster;

    @BeforeEach
    void beforeEach() {
        ontologyConnector = new OntologyConnector(ontologyPath);
        var testClusterIndividual = ontologyConnector.getIndividualByIri(testClusterUri).orElseThrow();
        ontologyCorefCluster = OntologyCorefCluster.get(ontologyConnector, testClusterIndividual);
    }

    @AfterEach
    void afterEach() {
        ontologyConnector = null;
        ontologyCorefCluster = null;
    }

    @Test
    @DisplayName("Test getId()")
    void getIdTest() {
        var id = ontologyCorefCluster.id();
        Assertions.assertEquals(198, id);
    }

    @Test
    @DisplayName("Test getRepresentativeMention()")
    void getRepresentativeMentionTest() {
        var reprMention = ontologyCorefCluster.representativeMention();
        Assertions.assertEquals("one single registry", reprMention);
    }

    @Test
    @DisplayName("Test getMentions()")
    void getMentionsTest() {
        var mentions = ontologyCorefCluster.mentions();
        Assertions.assertNotNull(mentions);
        Assertions.assertEquals(2, mentions.size());

        Assertions.assertEquals(3, mentions.get(0).size());
        Assertions.assertEquals(1, mentions.get(1).size());
    }
}
