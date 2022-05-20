/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.base.SentenceTest;

class JsonSentenceTest extends SentenceTest {
    @Override
    protected ITextConnector getProvider() {
        return JsonTextProviderTest.getTextProvider();
    }
}
