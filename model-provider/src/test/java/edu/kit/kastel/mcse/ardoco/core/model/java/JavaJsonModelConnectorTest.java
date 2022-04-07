/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.model.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

class JavaJsonModelConnectorTest {
    @Test
    @DisplayName("Simply test loading of TEASTORE")
    void testLoading() throws IOException {
        InputStream is = Objects.requireNonNull(JavaJsonModelConnectorTest.class.getResourceAsStream("/TeaStore.json"));
        JavaJsonModelConnector jjmc = new JavaJsonModelConnector(is);
        Assertions.assertEquals(192, jjmc.getInstances().size());
        // NIY
        Assertions.assertEquals(0, jjmc.getRelations().size());
        Assertions.assertEquals(Metamodel.CODE, jjmc.getMetamodel());

    }
}
