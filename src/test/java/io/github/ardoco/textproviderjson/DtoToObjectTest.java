package io.github.ardoco.textproviderjson;

import io.github.ardoco.textproviderjson.converter.DtoToObjectConverter;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class DtoToObjectTest {

    @Test
    void convertTextTest() throws IOException {
        DtoToObjectConverter converter = new DtoToObjectConverter();
        Text expected = TestUtil.generateDefaultText();
        Text actual = converter.convertText(TestUtil.generateDefaultDTO());
        Assertions.assertEquals(expected, actual);
    }
}
