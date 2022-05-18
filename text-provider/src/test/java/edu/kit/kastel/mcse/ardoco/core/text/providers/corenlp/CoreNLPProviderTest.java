/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CoreNLPProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(CoreNLPProviderTest.class);
    protected static String inputText = "src/test/resources/teastore.txt";

    private static CoreNLPProvider coreNLPProvider = null;

    @BeforeAll
    static void beforeAll() {
        coreNLPProvider = getCoreNLPProvider();
    }

    public synchronized static CoreNLPProvider getCoreNLPProvider() {
        if (coreNLPProvider == null) {
            try {
                coreNLPProvider = new CoreNLPProvider(new FileInputStream(inputText));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return coreNLPProvider;
    }

    @Test
    void getTextTest() {
        var text = coreNLPProvider.getAnnotatedText();
        Assertions.assertNotNull(text);
    }
}
