/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.ardoco.textproviderjson.converter.ObjectToDtoConverter;
import io.github.ardoco.textproviderjson.dto.TextDTO;

class ObjectToDtoTest {

    @Test
    void convertObjectToDtoTest() throws IOException {
        ObjectToDtoConverter converter = new ObjectToDtoConverter();
        TextDTO expected = TestUtil.generateDefaultDTO();
        TextDTO actual = converter.convertTextToDTO(TestUtil.generateDefaultText());
        Assertions.assertEquals(expected, actual);
    }

}
