/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.TextProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.base.ProviderTest;

class CoreNLPProviderTest extends ProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(CoreNLPProviderTest.class);
    protected static String inputText = "src/test/resources/teastore.txt";

    private static CoreNLPProvider coreNLPProvider = null;

    public synchronized static CoreNLPProvider getCoreNLPProvider() {
        if (coreNLPProvider == null) {
            try {
                coreNLPProvider = new CoreNLPProvider(new DataRepository(), new FileInputStream(inputText));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return coreNLPProvider;
    }

    @Override
    protected TextProvider getProvider() {
        return CoreNLPProviderTest.getCoreNLPProvider();
    }
}
