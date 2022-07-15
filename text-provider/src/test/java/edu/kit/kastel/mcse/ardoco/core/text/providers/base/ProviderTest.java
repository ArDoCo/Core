/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.TextProvider;

public abstract class ProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(ProviderTest.class);
    protected static String inputText = "src/test/resources/teastore.txt";

    private TextProvider provider = null;

    @BeforeEach
    void beforeEach() {
        provider = getProvider();
    }

    protected abstract TextProvider getProvider();

    @Test
    void getTextTest() {
        var text = provider.getAnnotatedText();
        Assertions.assertNotNull(text);
    }
}
