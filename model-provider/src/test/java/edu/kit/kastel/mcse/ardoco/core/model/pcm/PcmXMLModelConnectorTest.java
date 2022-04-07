/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.model.pcm;

import java.io.InputStream;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

class PcmXMLModelConnectorTest {
    @Test
    void testLoadMediaStore() throws Exception {
        InputStream is = Objects.requireNonNull(PcmXMLModelConnectorTest.class.getResourceAsStream("/ms.repository"));
        PcmXMLModelConnector connector = new PcmXMLModelConnector(is);
        is.close();

        Assertions.assertEquals("_7zbcYHDhEeSqnN80MQ2uGw", connector.getModelId());
        Assertions.assertEquals(Metamodel.ARCHITECTURE, connector.getMetamodel());
        Assertions.assertEquals(14, connector.getInstances().size());
        Assertions.assertTrue(connector.getInstances().allSatisfy(i -> i.getFullType().equals("BasicComponent")));
        // NIY
        Assertions.assertEquals(0, connector.getRelations().size());

    }
}
