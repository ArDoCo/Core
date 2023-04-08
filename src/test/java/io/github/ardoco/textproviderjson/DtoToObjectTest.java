/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import io.github.ardoco.textproviderjson.converter.DtoToObjectConverter;

class DtoToObjectTest {

    @Test
    void convertTextTest() throws IOException {
        DtoToObjectConverter converter = new DtoToObjectConverter();
        Text expected = TestUtil.generateDefaultText();
        Text actual = converter.convertText(TestUtil.generateDefaultDTO());
        Assertions.assertEquals(expected, actual);
    }
}
