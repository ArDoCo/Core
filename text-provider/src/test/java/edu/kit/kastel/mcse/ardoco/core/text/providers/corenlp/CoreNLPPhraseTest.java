/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.TextProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.base.PhraseTest;

class CoreNLPPhraseTest extends PhraseTest {

    @Override
    protected TextProvider getProvider() {
        return CoreNLPProviderTest.getCoreNLPProvider();
    }
}
