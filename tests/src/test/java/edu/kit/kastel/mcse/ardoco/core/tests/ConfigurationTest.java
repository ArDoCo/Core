/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.AbstractConfigurable;
import edu.kit.kastel.informalin.framework.configuration.Configurable;

/**
 * This test class deals with the configurations.
 *
 * @see AbstractConfigurable
 */
class ConfigurationTest {
    /**
     * This test verifies that all configurable values are able to be configured. It also prints all configurable values
     * as they should be contained in a configuration file.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    void showCurrentConfiguration() throws Exception {
        Map<String, String> configs = new TreeMap<>();
        var reflectAccess = new Reflections("edu.kit.kastel.mcse.ardoco");
        var classesThatMayBeConfigured = reflectAccess.getSubTypesOf(AbstractConfigurable.class)
                .stream()
                .filter(c -> c.getPackageName().startsWith("edu.kit.kastel.mcse.ardoco"))
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .filter(c -> !c.getPackageName().contains("tests"))
                .toList();
        for (var clazz : classesThatMayBeConfigured) {
            processConfigurationOfClass(configs, clazz);
        }
        Assertions.assertFalse(configs.isEmpty());

        System.out.println("-".repeat(50));
        System.out.println("Current Default Configuration");
        System.out.println(configs.entrySet()
                .stream()
                .map(e -> e.getKey() + AbstractConfigurable.KEY_VALUE_CONNECTOR + e.getValue())
                .collect(Collectors.joining("\n")));
        System.out.println("-".repeat(50));
    }

    @Test
    void testValidityOfConfigurableFields() throws Exception {
        var reflectAccess = new Reflections("edu.kit.kastel.mcse.ardoco");
        var classesThatMayBeConfigured = reflectAccess.getSubTypesOf(AbstractConfigurable.class)
                .stream()
                .filter(c -> c.getPackageName().startsWith("edu.kit.kastel.mcse.ardoco"))
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .filter(c -> !c.getPackageName().contains("tests"))
                .toList();

        for (var clazz : classesThatMayBeConfigured) {
            List<Field> configurableFields = new ArrayList<>();
            findImportantFields(clazz, configurableFields);

            for (var field : configurableFields) {
                int modifiers = field.getModifiers();
                Assertions.assertFalse(Modifier.isFinal(modifiers), "Field " + field.getName() + "@" + field.getDeclaringClass()
                        .getSimpleName() + " is final!");
                Assertions.assertFalse(Modifier.isStatic(modifiers), "Field " + field.getName() + "@" + field.getDeclaringClass()
                        .getSimpleName() + " is static!");
            }
        }
    }

    @Test
    void testBasicConfigurable() throws Exception {
        Map<String, String> configs = new TreeMap<>();
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

    private void processConfigurationOfClass(Map<String, String> configs, Class<? extends AbstractConfigurable> clazz) throws InvocationTargetException,
            InstantiationException, IllegalAccessException {
        var object = createObject(clazz);
        List<Field> fields = new ArrayList<>();
        findImportantFields(object.getClass(), fields);
        fillConfigs(object, fields, configs);
    }

    private void fillConfigs(AbstractConfigurable object, List<Field> fields, Map<String, String> configs) throws IllegalAccessException {
        for (Field f : fields) {
            f.setAccessible(true);
            var key = f.getDeclaringClass().getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + f.getName();
            var rawValue = f.get(object);
            var value = getValue(rawValue);
            if (configs.containsKey(key)) {
                Assertions.fail("Found duplicate entry in map: " + key);
            }
            configs.put(key, value);
        }
    }

    private String getValue(Object rawValue) {
        if (rawValue instanceof Integer i) {
            return Integer.toString(i);
        }
        if (rawValue instanceof Double d) {
            return String.format(Locale.ENGLISH, "%f", d);
        }
        if (rawValue instanceof Boolean b) {
            return String.valueOf(b);
        }
        if (rawValue instanceof List<?> s && s.stream().allMatch(it -> it instanceof String)) {
            return s.stream().map(Object::toString).collect(Collectors.joining(AbstractConfigurable.LIST_SEPARATOR));
        }
        if (rawValue instanceof Enum<?> e) {
            return e.name();
        }

        throw new IllegalArgumentException("RawValue has no type that may be transformed to an Configuration" + rawValue + "[" + rawValue.getClass() + "]");

    }

    private void findImportantFields(Class<?> clazz, List<Field> fields) {
        if (clazz == Object.class || clazz == AbstractConfigurable.class) {
            return;
        }

        for (var field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Configurable.class)) {
                fields.add(field);
            }
        }
        findImportantFields(clazz.getSuperclass(), fields);
    }

    private AbstractConfigurable createObject(Class<? extends AbstractConfigurable> clazz) throws InvocationTargetException, InstantiationException,
            IllegalAccessException {
        var constructors = Arrays.asList(clazz.getDeclaredConstructors());
        if (constructors.stream().anyMatch(c -> c.getParameterCount() == 0)) {
            var constructor = constructors.stream().filter(c -> c.getParameterCount() == 0).findFirst().get();
            constructor.setAccessible(true);
            return (AbstractConfigurable) constructor.newInstance();
        }
        if (constructors.stream().anyMatch(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == Map.class)) {
            var constructor = constructors.stream().filter(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == Map.class).findFirst().get();
            constructor.setAccessible(true);
            return (AbstractConfigurable) constructor.newInstance(Map.of());
        }
        if (constructors.stream().anyMatch(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == DataRepository.class)) {
            var constructor = constructors.stream()
                    .filter(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == DataRepository.class)
                    .findFirst()
                    .get();
            constructor.setAccessible(true);
            return (AbstractConfigurable) constructor.newInstance(new Object[] { null });
        }

        Assertions.fail("No suitable constructor has been found for " + clazz);
        throw new Error("Not reachable code");
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
