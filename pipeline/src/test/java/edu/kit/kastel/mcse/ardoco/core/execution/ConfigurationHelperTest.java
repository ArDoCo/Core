/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;

class ConfigurationHelperTest {

    @Test
    void getDefaultConfigurationOptionsTest() {
        var configs = ConfigurationHelper.getDefaultConfigurationOptions();
        Assertions.assertNotNull(configs);

        for (var entry : configs.entrySet()) {
            Assertions.assertAll(//
                    () -> Assertions.assertNotNull(entry.getKey()), //
                    () -> Assertions.assertNotNull(entry.getValue()));
        }
    }

    @Test
    void testBasicConfigurable() throws Exception {
        Map<String, String> configs = new TreeMap<>();
        ConfigurationHelper.processConfigurationOfClass(configs, TestConfigurable.class);
        Assertions.assertEquals(5, configs.size());

        var t = new TestConfigurable();

        Assertions.assertEquals(24, t.testInt);
        Assertions.assertEquals(24, t.testIntNo);
        Assertions.assertEquals(2.0, t.testDouble);
        Assertions.assertEquals(2.0, t.testDoubleNo);
        Assertions.assertTrue(t.testBool);
        Assertions.assertTrue(t.testBoolNo);
        Assertions.assertEquals(List.of("A", "B", "C"), t.testList);
        Assertions.assertEquals(List.of("A", "B", "C"), t.testListNo);
        Assertions.assertEquals(TestConfigurable.MyEnum.A, t.testEnum);
        Assertions.assertEquals(TestConfigurable.MyEnum.B, t.testEnumNo);

        //@formatter:off
        configs = Map.of(//
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testInt", "42", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testIntNo", "42", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testDouble", "48", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testDoubleNo", "48", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testBool", "false", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testBoolNo", "false", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testList", String.join(AbstractConfigurable.LIST_SEPARATOR, "X", "Y", "Z"), //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testListNo", String.join(AbstractConfigurable.LIST_SEPARATOR, "X", "Y", "Z"), //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testEnum", TestConfigurable.MyEnum.C.name(), //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testEnumNo", TestConfigurable.MyEnum.C.name()

        );
        //@formatter:on

        t.applyConfiguration(configs);
        Assertions.assertEquals(42, t.testInt);
        Assertions.assertEquals(24, t.testIntNo);
        Assertions.assertEquals(48, t.testDouble);
        Assertions.assertEquals(2.0, t.testDoubleNo);
        Assertions.assertFalse(t.testBool);
        Assertions.assertTrue(t.testBoolNo);
        Assertions.assertEquals(List.of("X", "Y", "Z"), t.testList);
        Assertions.assertEquals(List.of("A", "B", "C"), t.testListNo);
        Assertions.assertEquals(TestConfigurable.MyEnum.C, t.testEnum);
        Assertions.assertEquals(TestConfigurable.MyEnum.B, t.testEnumNo);

    }

    private static final class TestConfigurable extends AbstractConfigurable {

        @Configurable
        private int testInt = 24;
        @Configurable
        private double testDouble = 2.0;
        @Configurable
        private boolean testBool = true;
        @Configurable
        private List<String> testList = List.of("A", "B", "C");
        @Configurable
        private MyEnum testEnum = MyEnum.A;

        private int testIntNo = 24;
        private double testDoubleNo = 2.0;
        private boolean testBoolNo = true;
        private List<String> testListNo = List.of("A", "B", "C");
        private MyEnum testEnumNo = MyEnum.B;

        public TestConfigurable() {
            // NOP
        }

        @Override
        protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        }

        private enum MyEnum {
            A, B, C
        }
    }
}
