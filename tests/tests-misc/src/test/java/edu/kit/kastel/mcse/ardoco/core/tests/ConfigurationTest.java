/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ConfigurationTestBase;

/**
 * This test class deals with the configurations.
 *
 * @see AbstractConfigurable
 */
class ConfigurationTest extends ConfigurationTestBase {
    @Override
    protected void assertFalse(boolean result, String message) {
        Assertions.assertFalse(result, message);
    }

    @Override
    protected void fail(String message) {
        Assertions.fail(message);
    }

    /**
     * This test verifies that all configurable values are able to be configured. It also prints all configurable values
     * as they should be contained in a configuration file.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void showCurrentConfiguration() throws Exception {
        super.showCurrentConfiguration();
    }

    @Test
    public void testValidityOfConfigurableFields() {
        super.testValidityOfConfigurableFields();
    }

    @Test
    void testBasicConfigurable() throws Exception {
        SortedMap<String, String> configs = new TreeMap<>();
        processConfigurationOfClass(configs, TestConfigurable.class);
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
        configs = new TreeMap<>(Map.of(//
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

        ));
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
        protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        }

        private enum MyEnum {
            A, B, C
        }
    }
}
