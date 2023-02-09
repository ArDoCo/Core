/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import static edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities.readInputText;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.text.providers.base.NlpInformantTest;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.CoreNLPProvider;

class CoreNLPProviderTest extends NlpInformantTest {
    private static final Logger logger = LoggerFactory.getLogger(CoreNLPProviderTest.class);
    protected static String inputText = "src/test/resources/teastore.txt";

    private static CoreNLPProvider coreNLPProvider = null;

    public synchronized static CoreNLPProvider getCoreNLPProvider() {
        if (coreNLPProvider == null) {
            try {
                DataRepository dataRepository = new DataRepository();
                DataRepositoryHelper.putInputText(dataRepository, readInputText(new FileInputStream(inputText)));
                coreNLPProvider = new CoreNLPProvider(dataRepository);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return coreNLPProvider;
    }

    @Override
    protected CoreNLPProvider getProvider() {
        return CoreNLPProviderTest.getCoreNLPProvider();
    }
}
