package io.github.ardoco.textproviderjson;

import io.github.ardoco.textproviderjson.converter.ObjectToDtoConverter;
import io.github.ardoco.textproviderjson.dto.TextDTO;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ObjectToDtoTest {

    @Test
    void convertObjectToDtoTest() throws IOException {
        ObjectToDtoConverter converter = new ObjectToDtoConverter();
        TextDTO expected = TestUtil.generateDefaultDTO();
        TextDTO actual = converter.convertTextToDTO(TestUtil.generateDefaultText());
        Assertions.assertEquals(expected, actual);
    }

}
