package edu.kit.kastel.mcse.ardoco.core.util;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceAccessorTest {

    private ResourceAccessor resourceAccessor;

    @BeforeEach
    void setup() {
        resourceAccessor = new ResourceAccessor("src/test/resources/TestConfig.properties", false);
    }

    @Test
    void getPropertyTest() {
        String prop = resourceAccessor.getProperty("stringProperty");
        Assertions.assertEquals("testString", prop);
    }

    @Test
    void getIntPropertyTest() {
        int prop = resourceAccessor.getPropertyAsInt("intProperty");
        Assertions.assertEquals(42, prop);

        prop = resourceAccessor.getPropertyAsInt("negativeIntProperty");
        Assertions.assertEquals(-42, prop);
    }

    @Test
    void getDoublePropertyTest() {
        double prop = resourceAccessor.getPropertyAsDouble("doubleProperty");
        Assertions.assertEquals(0.42, prop, 0.01);
    }

    @Test
    void isPropertyEnabledTest() {
        boolean prop = resourceAccessor.isPropertyEnabled("booleanProperty");
        Assertions.assertTrue(prop);

        prop = resourceAccessor.isPropertyEnabled("yesProperty");
        Assertions.assertTrue(prop);

        prop = resourceAccessor.isPropertyEnabled("oneProperty");
        Assertions.assertTrue(prop);

        prop = resourceAccessor.isPropertyEnabled("noProperty");
        Assertions.assertFalse(prop);
    }

    @Test
    void getListPropertyTest() {
        List<String> prop = resourceAccessor.getPropertyAsList("listProperty").castToList();
        Assertions.assertArrayEquals(new String[] { "first", "second", "third" }, prop.toArray());
    }

}
