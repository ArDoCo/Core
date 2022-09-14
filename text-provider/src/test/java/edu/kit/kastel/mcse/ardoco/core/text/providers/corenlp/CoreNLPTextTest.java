/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.TextProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.base.TextTest;

class CoreNLPTextTest extends TextTest {
    @Override
    protected TextProvider getProvider() {
        return CoreNLPProviderTest.getCoreNLPProvider();
    }
}
