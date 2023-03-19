package io.github.ardoco.textproviderjson;

import io.github.ardoco.textproviderjson.converter.DTOtoObjConverter;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class DTOToObjectTest {

    @Test
    void convertTextTest() throws IOException {
        DTOtoObjConverter converter = new DTOtoObjConverter();
        Text expected = TestUtil.generateDefaultText();
        Text actual = converter.convertText(TestUtil.generateDefaultDTO());
        Assertions.assertEquals(expected, actual);
    }
}
