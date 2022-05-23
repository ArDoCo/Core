/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.base.PhraseTest;

class CoreNLPPhraseTest extends PhraseTest {

    @Override
    protected ITextConnector getProvider() {
        return CoreNLPProviderTest.getCoreNLPProvider();
    }
}
