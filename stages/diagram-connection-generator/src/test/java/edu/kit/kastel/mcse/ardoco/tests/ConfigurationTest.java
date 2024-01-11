/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ConfigurationTestBase;

public class ConfigurationTest extends ConfigurationTestBase {
    @Override
    protected void assertFalse(boolean result, String message) {
        Assertions.assertFalse(result, message);
    }

    @Override
    protected void fail(String message) {
        Assertions.fail(message);
    }

    @Override
    @Test
    public void showCurrentConfiguration() throws Exception {
        super.showCurrentConfiguration();
    }

    @Override
    @Test
    public void testValidityOfConfigurableFields() {
        super.testValidityOfConfigurableFields();
    }
}
