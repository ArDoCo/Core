/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.File;

import org.junit.jupiter.api.*;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.ICorefCluster;

class JsonCorefClusterTest {
    protected static String path = "src/test/resources/teastore.json";

    protected JsonTextProvider connector;
    protected ICorefCluster corefCluster;

    @BeforeEach
    void beforeEach() throws Exception {
        // Cluster "one single registry"
        connector = JsonTextProvider.loadFromFile(new File(path));
        corefCluster = connector.getAnnotatedText()
                .getCorefClusters()
                .select(p -> p.representativeMention().equals("one single registry"))
                .stream()
                .findFirst()
                .orElseThrow();
    }

    @AfterEach
    void afterEach() {
        connector = null;
        corefCluster = null;
    }

    @Test
    @DisplayName("Test getId()")
    void getIdTest() {
        var id = corefCluster.id();
        Assertions.assertEquals(196, id);
    }

    @Test
    @DisplayName("Test getRepresentativeMention()")
    void getRepresentativeMentionTest() {
        var reprMention = corefCluster.representativeMention();
        Assertions.assertEquals("one single registry", reprMention);
    }

    @Test
    @DisplayName("Test getMentions()")
    void getMentionsTest() {
        var mentions = corefCluster.mentions();
        Assertions.assertNotNull(mentions);
        Assertions.assertEquals(2, mentions.size());

        Assertions.assertEquals(3, mentions.get(0).size());
        Assertions.assertEquals(1, mentions.get(1).size());
    }
}
