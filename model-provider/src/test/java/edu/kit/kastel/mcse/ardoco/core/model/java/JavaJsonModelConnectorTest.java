/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.model.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.model.JavaJsonModelConnector;

class JavaJsonModelConnectorTest {
    @Test
    @DisplayName("Simply test loading of TEASTORE")
    void testLoading() throws IOException {
        InputStream is = Objects.requireNonNull(JavaJsonModelConnectorTest.class.getResourceAsStream("/teastore-code.json"));
        JavaJsonModelConnector jjmc = new JavaJsonModelConnector(is);
        Assertions.assertEquals(192, jjmc.getInstances().size());
        Assertions.assertEquals("3368242f-d572-3d92-9031-dc20a86dcff1", jjmc.getModelId());
        Assertions.assertEquals(Metamodel.CODE, jjmc.getMetamodel());
    }
}
